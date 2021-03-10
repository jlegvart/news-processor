import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaObjectMapper}
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import model.{Channel, RSSFeed}

import scala.io.Source

object Test extends App {

  val str = Source.fromResource("source.xml").getLines().mkString

  val module = new JacksonXmlModule
  module.setDefaultUseWrapper(false)

  val mapper = new XmlMapper(module)
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

  val feed = mapper.readValue(str, classOf[RSSFeed])

  println(feed)

  assert(feed.channel.item.size == 55)
}
