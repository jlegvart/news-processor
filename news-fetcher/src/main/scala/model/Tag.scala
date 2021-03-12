package model

object Tag {

  val tags = Seq(
    Tag("politics", Seq("politics")),
    Tag("coronavirus", Seq("coronavirus", "covid", "covid19", "covid-19")),
    Tag("health", Seq("health", "coronavirus", "covid", "covid19", "covid-19")),
    Tag("science", Seq("science")),
    Tag("education", Seq("education")),
    Tag("economy", Seq("economy", "business", "stock")),
    Tag("technology", Seq("technology")),
    Tag("entertainment", Seq("entertainment")),
    Tag("sport", Seq("sport")),

    Tag("europe", Seq("europe")),
    Tag("asia", Seq("asia")),
    Tag("china", Seq("china")),
    Tag("uk", Seq("uk")),
    Tag("world", Seq("world")),
    Tag("africa", Seq("africa")),
    Tag("us", Seq("us", "usa")),

  )

}

case class Tag(tagName: String, keywords: Seq[String])
