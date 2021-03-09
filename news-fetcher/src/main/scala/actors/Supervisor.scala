package actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Supervisor {

  def apply(): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => new Supervisor(context))
}


class Supervisor(context: ActorContext[Nothing]) extends AbstractBehavior[Nothing](context) {

  context.log.debug("Starting Supervisor actor")

  override def onMessage(msg: Nothing): Behavior[Nothing] = Behaviors.unhandled
}