package persistence

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, Response}
import config.AppConfig
import model.FeedArticle

import scala.concurrent.Future

object Elastic {

  val client = Elastic()
}


case class Elastic() {

  val indexArticles = "articles"

  implicit val objectMapper: ObjectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)
  objectMapper.registerModule(new JavaTimeModule())

  objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

  val props = ElasticProperties(AppConfig.config.getString("elasticsearch.host"))
  val client = ElasticClient(JavaClient(props))

  def index(article: FeedArticle): Future[Response[IndexResponse]] = {
    client.execute {
      indexInto(indexArticles).doc(article)
    }
  }
}
