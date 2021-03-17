package actors

import actors.Messages.Content.GetFeed
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import config.AppConfig
import model.FeedSource

import java.util
import scala.jdk.javaapi.CollectionConverters


object Fetcher {

  def apply(cleanerActor: ActorRef[Messages.Cleaner.Command]): Behavior[_] =
    Behaviors.setup[Any](context => new Fetcher(context, cleanerActor))
}

class Fetcher(context: ActorContext[Any], cleanerActor: ActorRef[Messages.Cleaner.Command]) extends AbstractBehavior[Any](context) {

  context.log.debug("Starting Fetcher actor")

  spawnContentProviderActors(cleanerActor)

  override def onMessage(msg: Any): Behavior[Any] = Behaviors.unhandled

  def spawnContentProviderActors(cleanerActor: ActorRef[Messages.Cleaner.Command]) = {
    newsSources().map { source =>
      val actor = context.spawn(ContentProvider(cleanerActor, source), s"contentProviderActor-${source.key}")
      actor ! GetFeed
    }
  }

  def newsSources(): Seq[FeedSource] = {
    val news = AppConfig.config.getObject("news")

    CollectionConverters.asScala(news.keySet()).toSeq.map(s =>
      FeedSource(s, CollectionConverters.asScala(news.get(s).unwrapped().asInstanceOf[util.ArrayList[String]]).head))
  }
}
