package service

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.util.ByteString
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.{JacksonXmlModule, XmlMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import model.RSSFeed
import org.slf4j.LoggerFactory

import scala.concurrent.Future

sealed trait ContentProviderServiceResponse

case class RSSContentProviderResponse() extends ContentProviderServiceResponse {}

case class ContentProviderException(reason: String) extends Throwable(reason)


sealed trait ContentProviderService {

  def getFeed(url: String): Future[RSSFeed]

  def getArticle(url: String): Unit
}

case class RSSContentProviderService() extends ContentProviderService {

  val log = LoggerFactory.getLogger(classOf[RSSContentProviderService])

  implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext = system.executionContext

  val module = new JacksonXmlModule
  module.setDefaultUseWrapper(false)

  val mapper = new XmlMapper(module)
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

  val http = Http(system)

  override def getFeed(url: String): Future[RSSFeed] = {
    log.debug(s"Requesting Feed from ${url}")

    http.singleRequest(HttpRequest(uri = url)).flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        log.debug(s"Received OK (200) for Feed request: ${url}")

        entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { body =>
          mapper.readValue(body.utf8String, classOf[RSSFeed])
        }

      case resp@HttpResponse(code, _, _, _) =>
        resp.discardEntityBytes()
        Future.failed(ContentProviderException(s"Feed retrieval from ${url} failed. Response code: ${code}"))
    }
  }

  override def getArticle(url: String) = ???
}