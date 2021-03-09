package actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}


object Fetcher {

  def apply(): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => new Fetcher(context))
}

class Fetcher(context: ActorContext[Nothing]) extends AbstractBehavior[Nothing](context) {

  context.log.debug("Starting Fetcher actor")

  //Spawn actors

  override def onMessage(msg: Nothing): Behavior[Nothing] = Behaviors.unhandled
}
