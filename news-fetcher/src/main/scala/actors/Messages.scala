package actors

import akka.http.scaladsl.model.HttpResponse
import model.{Channel, RSSFeed}

object Messages {

  object Content {
    sealed trait Command

    case object GetFeed extends Command
    case class GetArticle() extends Command

    sealed trait FeedResponse
    case class FeedResponseError(e: Throwable) extends FeedResponse
    case class FeedResponseSuccess(response: HttpResponse) extends FeedResponse

    case class ProcessFeedResponse(feedResponse: FeedResponse) extends Command

    case class ProcessFeedArticles(feed: RSSFeed) extends Command

    sealed trait ArticleResponse
    case class ArticleResponseSuccess(response: HttpResponse) extends ArticleResponse
    case class ArticleResponseError(e: Throwable) extends ArticleResponse
    case class ProcessArticleResponse(response: ArticleResponse) extends Command
  }

  object Cleaner {
    sealed trait Command

    case class ProcessArticle() extends Command
  }

  object Persistence {
    sealed trait Command

    case class Persist() extends Command
  }


}
