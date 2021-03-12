package actors

import akka.http.scaladsl.model.HttpResponse
import model.{Channel, FeedArticle, Item, RSSFeed}

object Messages {

  object Content {
    sealed trait Command

    case object GetFeed extends Command

    sealed trait FeedResponse
    case class FeedResponseError(e: Throwable) extends FeedResponse
    case class FeedResponseSuccess(response: HttpResponse) extends FeedResponse

    case class ProcessFeedResponse(feedResponse: FeedResponse) extends Command

    case object ProcessFeedArticle extends Command

    sealed trait ArticleResponse
    case class ArticleResponseSuccess(response: HttpResponse) extends ArticleResponse
    case class ArticleResponseError(e: Throwable) extends ArticleResponse

    case class ProcessArticleResponse(item: Item, attempt: Int, response: ArticleResponse) extends Command
  }

  object Cleaner {
    sealed trait Command

    case class ProcessArticle(article: FeedArticle) extends Command
  }

  object Persistence {
    sealed trait Command

    case class Persist(article: FeedArticle) extends Command
  }


}
