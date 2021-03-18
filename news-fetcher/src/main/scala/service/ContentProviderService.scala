package service

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.util.ByteString
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.{JacksonXmlModule, XmlMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import model.{FeedArticle, Item, RSSFeed}
import org.slf4j.LoggerFactory

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

sealed trait ContentProviderServiceResponse

case class RSSContentProviderResponse() extends ContentProviderServiceResponse {}

case class ContentProviderException(reason: String) extends Throwable(reason)


sealed trait ContentProviderService {

  def getFeed(url: String): Future[RSSFeed]

  def getArticle(sourceKey: String, feedTitle: String, url: String, item: Item, attempt: Int): Future[FeedArticle]
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

  override def getArticle(sourceKey: String, feedTitle: String, url: String, item: Item, attempt: Int): Future[FeedArticle] = {
    log.debug(s"Sending request ${url}")

    http.singleRequest(HttpRequest(uri = url)).flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        log.debug(s"Received OK (200) for ${url}")

        entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { body =>
          FeedArticle(sourceKey, feedTitle, item.title, item.description, item.link, item.guid, body.utf8String,
            OffsetDateTime.parse(item.pubDate, DateTimeFormatter.RFC_1123_DATE_TIME), OffsetDateTime.now(), Seq.empty)
        }

      case resp@HttpResponse(StatusCodes.Found, headers, _, _) =>
        log.warn(s"Received Found (302) redirect for url: ${url}")

        if (attempt < 3) {
          resp.discardEntityBytes()
          headers.find(header => header.name().equals("Location")).map { header =>
            getArticle(sourceKey, feedTitle, header.value(), item, attempt + 1)
          }.getOrElse {
            Future.failed(new RuntimeException(""))
          }
        } else {
          log.error(s"Number of max redirect attempts reached (${attempt}), skipping article")
          resp.discardEntityBytes()

          Future.failed(new RuntimeException(""))
        }

      case resp@HttpResponse(code, _, _, _) =>
        log.error(s"Request failed for link: ${item.link} with code: ${code}")
        resp.discardEntityBytes()

        Future.failed(new RuntimeException(""))
    }
  }
}