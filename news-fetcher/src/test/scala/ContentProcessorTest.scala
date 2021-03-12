import contentprocessor.ContentProcessor
import model.FeedArticle
import org.scalatest.FunSuite

import java.time.OffsetDateTime
import scala.io.Source

class ContentProcessorTest extends FunSuite {

  test("ContentProcessor.processTags") {
    assert(ContentProcessor.processTags("https://www.bbc.com/news/uk-56368531") === Set("uk"))
    assert(ContentProcessor.processTags("https://www.bbc.com/news/av/world-africa-56365847") === Set("world", "africa"))
    assert(ContentProcessor.processTags("https://www.bbc.com/news/uk-politics-56364306") === Set("politics", "uk"))
    assert(ContentProcessor.processTags("https://www.bbc.com/news/science-environment-56366107") === Set("science"))
    assert(ContentProcessor.processTags("https://www.bbc.com/news/technology-56362174") === Set("technology"))
    assert(ContentProcessor.processTags("https://www.bbc.com/news/world-europe-56344311") === Set("europe", "world"))
    assert(ContentProcessor.processTags("https://www.bbc.com/news/entertainment-arts-56230566") === Set("entertainment"))
    assert(ContentProcessor.processTags("https://www.bbc.com/sport/football/56351374") === Set("sport"))
    assert(ContentProcessor.processTags("https://www.bbc.com/news/av/health-56359242") === Set("health"))
    assert(ContentProcessor.processTags("https://www.bbc.com/news/world-asia-china-56364912") === Set("asia", "china", "world"))


    assert(ContentProcessor.processTags("https://edition.cnn.com/2021/03/11/asia/myanmar-crimes-against-humanity-intl-hnk/index.html") === Set("asia"))
    assert(ContentProcessor.processTags("https://edition.cnn.com/2021/03/12/africa/students-kidnapped-kaduna-nigeria-intl/index.html") === Set("africa"))
    assert(ContentProcessor.processTags("https://edition.cnn.com/2021/03/12/politics/japan-prime-minister-us-visit/index.html") === Set("politics", "us"))
    assert(ContentProcessor.processTags("https://edition.cnn.com/2021/03/11/us/circle-of-hope-girls-ranch-abuse-charges/index.html") === Set("us"))

  }

  test("ContentProcessor.processContent") {
    assert(ContentProcessor.processContent(Source.fromResource("feed/bbc.html").getLines().mkString).isDefined)
    assert(ContentProcessor.processContent(Source.fromResource("feed/cnn.html").getLines().mkString).isDefined)

    assert(ContentProcessor.processContent("<html><body><h1/>no article</body></html>").isEmpty)
  }

}
