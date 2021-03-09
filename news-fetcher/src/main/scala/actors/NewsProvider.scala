package actors

import actors.Messages.News._
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import model.NewsSource


object NewsProvider {

  def apply(source: NewsSource): Behavior[Command] =
    Behaviors.setup[Command](context => new NewsProvider(context, source))
}

class NewsProvider(context: ActorContext[Command], source: NewsSource) extends AbstractBehavior[Command](context) {

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case GetFeed => ???
      case GetArticle() => ???

      case ProcessFeedResponse() => ???
      case ProcessArticleResponse() => ???

        this
    }
  }
}