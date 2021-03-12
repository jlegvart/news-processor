import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.{JacksonXmlModule, XmlMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import model.RSSFeed

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import scala.io.Source

object Test extends App {

  val str = Source.fromResource("feed.xml").getLines().mkString

  val module = new JacksonXmlModule
  module.setDefaultUseWrapper(false)

  val mapper = new XmlMapper(module)
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

  val feed = mapper.readValue(str, classOf[RSSFeed])

  feed.channel.item.foreach(println(_))

  assert(feed.channel.item.size == 55)

  val datetime = OffsetDateTime.parse("Wed, 10 Mar 2021 16:51:18 GMT", DateTimeFormatter.RFC_1123_DATE_TIME)

  println(datetime)
  println()
}
