package actors

import actors.Messages.Cleaner.{Command, ProcessArticle}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

object Cleaner {

  def apply(persistenceActor: ActorRef[Messages.Persistence.Command]): Behavior[Messages.Cleaner.Command] =
    Behaviors.setup[Messages.Cleaner.Command](context => new Cleaner(context, persistenceActor))

}

class Cleaner(context: ActorContext[Command], persistenceActor: ActorRef[Messages.Persistence.Command]) extends AbstractBehavior[Command](context) {

  val log = context.log

  log.debug("Starting Cleaner actor")

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case ProcessArticle(article) =>
        log.debug("Feed article received")
        this
    }
  }
}

