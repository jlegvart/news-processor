package actors

import actors.Messages.Persistence.Command
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Persistence {

  def apply(): Behavior[Command] =
    Behaviors.setup[Command](context => new Persistence(context))

}

class Persistence(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  context.log.debug("Starting Persistence actor")

  override def onMessage(msg: Command): Behavior[Command] = Behaviors.unhandled
}
