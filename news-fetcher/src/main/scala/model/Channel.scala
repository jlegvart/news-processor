package model

import com.fasterxml.jackson.dataformat.xml.annotation.{JacksonXmlProperty, JacksonXmlText}

case class RSSFeed(channel: Channel)
case class Item(title: String, description: String, link: String, guid: String, pubDate: String)
case class Channel(title: String, description: String, link: String, language: String, copyright: String, @JacksonXmlProperty item: Seq[Item])
