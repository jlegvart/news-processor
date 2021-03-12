package actors

import actors.Messages.Content._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.util.ByteString
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.{JacksonXmlModule, XmlMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import model.{FeedArticle, FeedSource, RSSFeed}

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
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

  var feed: RSSFeed = _

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    //Get RSS feed
    case GetFeed =>
      log.debug(s"Requesting Feed from ${source.source}")
      context.pipeToSelf(http.singleRequest(HttpRequest(uri = source.source))) {
        case Success(response) => ProcessFeedResponse(FeedResponseSuccess(response))
        case Failure(e) => ProcessFeedResponse(FeedResponseError(e))
      }
      this

    //Process Feed response
    case ProcessFeedResponse(response) =>
      log.debug("Received ProcessFeedResponse message")
      response match {
        case FeedResponseSuccess(httpResponse: HttpResponse) => httpResponse match {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
              feed = mapper.readValue(body.utf8String, classOf[RSSFeed])
              context.self ! ProcessFeedArticle
            }(context.executionContext)
            this

          case resp@HttpResponse(code, _, _, _) =>
            log.error(s"Request for RSS feed from ${source.source} failed, response code: " + code)
            resp.discardEntityBytes()
            this
        }

        case FeedResponseError(e) =>
          log.error("Error during feed retrieval")
          log.error("Error", e)
          this
      }

    //Loop through articles, get content and pipe response to self
    case ProcessFeedArticle =>
      log.debug("Received ProcessFeedArticle message")
      feed.channel.item.headOption.foreach { item =>
        log.debug(s"Sending request ${item.link}")

        feed = feed.copy(feed.channel.copy(item = feed.channel.item.tail))

        context.pipeToSelf(http.singleRequest(HttpRequest(uri = item.link))) {
          case Success(response) => ProcessArticleResponse(item, 1, ArticleResponseSuccess(response))
          case Failure(e) => ProcessArticleResponse(item, 1, ArticleResponseError(e))
        }
      }
      this

    case ProcessArticleResponse(item, attempt, response) =>
      log.debug("Received ProcessArticleResponse message")
      response match {
        case ArticleResponseSuccess(httpResponse: HttpResponse) => httpResponse match {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
              log.debug(s"Received response 200 for ${item.link}")
              val article = FeedArticle(source.key, feed.channel.title, item.title, item.description, item.link, item.guid, body.utf8String,
                OffsetDateTime.parse(item.pubDate, DateTimeFormatter.RFC_1123_DATE_TIME), OffsetDateTime.now(), Seq.empty)

              cleanerActor ! Messages.Cleaner.ProcessArticle(article)
            }(context.executionContext)

            context.self ! ProcessFeedArticle
            this

          case resp@HttpResponse(StatusCodes.Found, headers, _, _) =>
            log.warn(s"Received redirect for url: ${item.link}")

            if (attempt < 3) {
              headers.filter(header => header.name().equals("Location")).foreach { header =>
                log.debug(s"Following redirect: from ${item.link} to ${header.value()}")

                context.pipeToSelf(http.singleRequest(HttpRequest(uri = header.value()))) {
                  case Success(response) => ProcessArticleResponse(item, attempt + 1, ArticleResponseSuccess(response))
                  case Failure(e) => ProcessArticleResponse(item, attempt + 1, ArticleResponseError(e))
                }
              }
            } else {
              log.error(s"Number of max redirect attempts reached (${attempt}), skipping article")
              context.self ! ProcessFeedArticle
            }

            resp.discardEntityBytes()
            this

          case resp@HttpResponse(code, _, _, _) =>
            log.error(s"Request failed for link: ${item.link} with code: ${code}")
            resp.discardEntityBytes()
            context.self ! ProcessFeedArticle
            this
        }

        case ArticleResponseError(e) =>
          log.error("Error during article retrieval")
          log.error("Error", e)
          this
      }
  }
}