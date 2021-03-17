import actors.Supervisor
import akka.actor.typed.ActorSystem
import akka.event.slf4j.Logger

object Main extends scala.App {

  val log = Logger("MAIN")

  Main.run()

  def run(): Unit = {
    val actorSystem = ActorSystem[Nothing](Supervisor(), "actorSystem")
  }
}
