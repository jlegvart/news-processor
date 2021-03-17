package actors

import actors.Messages.Persistence._
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.sksamuel.elastic4s.{RequestFailure, RequestSuccess, Response}
import model.FeedArticle
import persistence.Elastic

import scala.util.{Failure, Success}

object Persistence {

  def apply(): Behavior[Command] =
    Behaviors.setup[Command](context => new Persistence(context))
}

class Persistence(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  context.log.debug("Starting Persistence actor")

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case Persist(article) =>
      context.log.debug(s"Persisting article ${article.guid}")
      context.pipeToSelf(Elastic.client.index(article)) {
        case Success(response) => ProcessPersistenceResponse(article, PersistenceResponseSuccess(response))
        case Failure(e) => ProcessPersistenceResponse(article, PersistenceResponseError(e))
      }
      this

    case ProcessPersistenceResponse(article, response) => response match {
      case PersistenceResponseSuccess(elasticResponse) => processElasticResponse(article, elasticResponse)
        this

      case PersistenceResponseError(e) =>
        context.log.error(s"Error during persisting article ${article.guid}", e)
        this
    }
  }

  def processElasticResponse(article: FeedArticle, response: Response[_]): Unit = {
    response match {
      case RequestSuccess(status, body, headers, result) =>
        context.log.debug(s"RequestSuccess received from elasticsearch for article id: ${article.guid}, status code: ${status}")
      case RequestFailure(status, body, headers, error) =>
        context.log.debug(s"RequestFailure received from elasticsearch for article id ${article.guid}, status code: ${status}")
        context.log.debug(s"Failure: ${body.orNull}")
    }
  }
}
