package actors

import actors.Messages.Content._
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import model.NewsSource


object ContentProvider {

  def apply(cleanerActor: ActorRef[Messages.Cleaner.Command], source: NewsSource): Behavior[Command] =
    Behaviors.setup[Command](context => new ContentProvider(context, cleanerActor, source))
}

class ContentProvider(context: ActorContext[Command], cleanerActor: ActorRef[Messages.Cleaner.Command], source: NewsSource) extends AbstractBehavior[Command](context) {

  context.log.debug(s"Starting ContentProvider actor: ${source.key}")

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case GetFeed =>
        context.log.debug("Received GetFeed message")
        this

      case GetArticle() => ???

      case ProcessFeedResponse() => ???

      case ProcessArticleResponse() => ???

        this
    }
  }
}