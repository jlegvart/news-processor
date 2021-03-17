package model

import java.time.OffsetDateTime

case class FeedArticle(key: String, sourceTitle: String, title: String, description: String, link: String, guid: String, content: String,
                       pubDate: OffsetDateTime, retrievedDate: OffsetDateTime, tags: Seq[String])
