package actors

object Messages {

  object News {
    sealed trait Command

    case object GetFeed extends Command
    case class GetArticle() extends Command

    case class ProcessFeedResponse() extends Command
    case class ProcessArticleResponse() extends Command
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
