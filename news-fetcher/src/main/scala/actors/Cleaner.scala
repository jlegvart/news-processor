package actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import Messages.Cleaner.Command

object Cleaner {

  def apply(): Behavior[Messages.Cleaner.Command] =
    Behaviors.setup[Messages.Cleaner.Command](context => new Cleaner(context))

}

class Cleaner(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  context.log.debug("Starting Cleaner actor")

  override def onMessage(msg: Command): Behavior[Command] = Behaviors.unhandled
}

