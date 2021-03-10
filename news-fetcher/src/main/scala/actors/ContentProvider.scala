package actors

import actors.Messages.Content._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.util.ByteString
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.{JacksonXmlModule, XmlMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import model.{FeedSource, RSSFeed}

import scala.util.{Failure, Success}


object ContentProvider {

  def apply(cleanerActor: ActorRef[Messages.Cleaner.Command], source: FeedSource): Behavior[Command] =
    Behaviors.setup[Command](context => new ContentProvider(context, cleanerActor, source))
}

class ContentProvider(context: ActorContext[Command], cleanerActor: ActorRef[Messages.Cleaner.Command], source: FeedSource) extends AbstractBehavior[Command](context) {

  implicit val system = context.system

  val http = Http(system)

  val log = context.log

  val module = new JacksonXmlModule
  module.setDefaultUseWrapper(false)

  val mapper = new XmlMapper(module)
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

  log.debug(s"Starting ContentProvider actor: ${source.key}")

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    //Get RSS feed
    case GetFeed =>
      log.debug("Received GetFeed message")
      context.pipeToSelf(http.singleRequest(HttpRequest(uri = "https://feeds.bbci.co.uk/news/rss.xml"))) {
        case Success(response) => ProcessFeedResponse(FeedResponseSuccess(response))
        case Failure(e) => ProcessFeedResponse(FeedResponseError(e))
      }
      this

    //Process Feed response
    case ProcessFeedResponse(response) => response match {
      //handle 200, etc...
      case FeedResponseSuccess(success: HttpResponse) =>
        success.entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
          context.self ! ProcessFeedArticles(mapper.readValue(body.utf8String, classOf[RSSFeed]))
        }(context.executionContext)
        this

      case FeedResponseError(e) => log.error("Error during GetFeed request")
        this
    }

    //Loop through articles, get content and pipe response to self
    case ProcessFeedArticles(feed) =>
      feed.channel.item.foreach { item =>
        log.debug(s"Sending request ${item.link}")

        context.pipeToSelf(http.singleRequest(HttpRequest(uri = item.link))) {
          case Success(response) => ProcessArticleResponse(ArticleResponseSuccess(response))
          case Failure(e) => ProcessArticleResponse(ArticleResponseError(e))
        }
        //todo implement scheduler
      }
      this

    case ProcessArticleResponse(response) => response match {
      //handle 200, etc...
      case ArticleResponseSuccess(success: HttpResponse) =>
        success.entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
          log.debug("Received response")
        }(context.executionContext)
        this
      case ArticleResponseError(e) =>
        log.error("Error during article retrieval")
        log.error("Error", e)
        this
    }
  }
}