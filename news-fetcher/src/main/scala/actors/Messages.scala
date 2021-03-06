package actors

import com.sksamuel.elastic4s.Response
import model.{FeedArticle, Item, RSSFeed}

object Messages {

  object Content {
    sealed trait Command

    case object GetFeed extends Command

    sealed trait FeedResponse
    case class FeedResponseError(e: Throwable) extends FeedResponse
    case class FeedResponseSuccess(feed: RSSFeed) extends FeedResponse

    case class ProcessFeedResponse(feedResponse: FeedResponse) extends Command

    case object ProcessFeedArticle extends Command

    sealed trait ArticleResponse
    case class ArticleResponseSuccess(article: FeedArticle) extends ArticleResponse
    case class ArticleResponseError(e: Throwable) extends ArticleResponse

    case class ProcessArticleResponse(item: Item, response: ArticleResponse) extends Command
  }

  object Cleaner {
    sealed trait Command

    case class ProcessArticle(article: FeedArticle) extends Command
  }

  object Persistence {
    sealed trait Command

    case class Persist(article: FeedArticle) extends Command
    case class ProcessPersistenceResponse(article: FeedArticle, persistenceResponse: PersistenceResponse) extends Command

    sealed trait PersistenceResponse
    case class PersistenceResponseSuccess(elasticResponse: Response[_]) extends PersistenceResponse
    case class PersistenceResponseError(e: Throwable) extends PersistenceResponse
  }


}
