package actors

import actors.Messages.Content._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.Http
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.{JacksonXmlModule, XmlMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import model.{FeedSource, RSSFeed}
import org.slf4j.LoggerFactory
import service.{ContentProviderException, RSSContentProviderService}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object ContentProvider {

  def apply(cleanerActor: ActorRef[Messages.Cleaner.Command], source: FeedSource): Behavior[Command] =
    Behaviors.setup[Command](context => new ContentProvider(context, cleanerActor, source))
}

class ContentProvider(context: ActorContext[Command], cleanerActor: ActorRef[Messages.Cleaner.Command], source: FeedSource) extends AbstractBehavior[Command](context) {

  val log = LoggerFactory.getLogger(classOf[ContentProvider])

  implicit val system = context.system

  val http = Http(system)

  val module = new JacksonXmlModule
  module.setDefaultUseWrapper(false)

  val mapper = new XmlMapper(module)
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

  context.log.debug(s"Starting ContentProvider actor: ${source.key}")

  val cacheSize = 100
  var feed: RSSFeed = _
  val scheduleDelay = 1 minute
  val contentProviderService = RSSContentProviderService()
  var cache: mutable.LinkedHashSet[String] = mutable.LinkedHashSet()

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    //Get RSS feed
    case GetFeed =>
      context.pipeToSelf(contentProviderService.getFeed(source.source)) {
        case Success(feed) => ProcessFeedResponse(FeedResponseSuccess(feed))
        case Failure(e) => ProcessFeedResponse(FeedResponseError(e))
      }
      this

    //Process Feed response
    case ProcessFeedResponse(response) =>
      context.log.debug(s"Scheduling next GetFeed iteration, dalay: ${scheduleDelay}")
      context.scheduleOnce(scheduleDelay, context.self, GetFeed)

      response match {
        case FeedResponseSuccess(responseFeed) =>
          feed = responseFeed
          context.self ! ProcessFeedArticle
          this

        case FeedResponseError(e: ContentProviderException) =>
          context.log.error("Error during feed retrieval", e)
          this
      }

    //Loop through articles, get content and pipe response to self
    case ProcessFeedArticle =>
      feed.channel.item.headOption.foreach { item =>
        feed = feed.copy(feed.channel.copy(item = feed.channel.item.tail))

        if (!cache.contains(item.guid)) {
          context.log.debug(s"Sending request ${item.link}")

          context.pipeToSelf(contentProviderService.getArticle(source.key, feed.channel.title, item.link, item, 1)) {
            case Success(response) => ProcessArticleResponse(item, ArticleResponseSuccess(response))
            case Failure(e) => ProcessArticleResponse(item, ArticleResponseError(e))
          }
        } else {
          context.log.debug(s"Article ${item.guid} already in cache")
          context.self ! ProcessFeedArticle
        }
      }
      this

    case ProcessArticleResponse(item, response) =>
      response match {
        case ArticleResponseSuccess(article) =>
          cacheItem(item.guid)
          cleanerActor ! Messages.Cleaner.ProcessArticle(article)
          context.self ! ProcessFeedArticle
          this

        case ArticleResponseError(e) =>
          context.log.error("Error during article retrieval", e)
          cacheItem(item.guid)
          context.self ! ProcessFeedArticle
          this
      }
  }

  private def cacheItem(guid: String): Unit = {
    if (cache.size >= cacheSize) {
      cache = cache.drop(1)
    }

    cache += guid
  }
}