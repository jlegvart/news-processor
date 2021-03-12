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

  test("ContentProcessor.processContent.bbc") {
    val bbc1 = FeedArticle("bbc", "BBC", "Sarah Everard case: Met Police faces watchdog investigation",
      "The Met is facing questions over its response to an alleged case of indecent exposure involving the 33-year-old's suspected killer.",
    "https://www.bbc.com/news/uk-56368531", "https://www.bbc.com/news/uk-56368531", Source.fromResource("feed/bbc.html").getLines().mkString,
      OffsetDateTime.now, OffsetDateTime.now, Seq.empty)

    println(ContentProcessor.processContent(bbc1).content)
  }

  test("ContentProcessor.processContent.cnn") {
    val cnn1 = FeedArticle("cnn", "CNN", "Trump's time in White House could end up benefiting New York prosecutors",
      "Former President Donald Trump's time in the White House helped shield him from investigations and some lawsuits but it might open him up to greater legal peril from New York prosecutors investigating his finances.",
      "https://edition.cnn.com/2021/03/12/politics/trump-statute-of-limitations/index.html", "https://edition.cnn.com/2021/03/12/politics/trump-statute-of-limitations/index.html",
      Source.fromResource("feed/cnn.html").getLines().mkString, OffsetDateTime.now, OffsetDateTime.now, Seq.empty)

    println(ContentProcessor.processContent(cnn1).content)
  }

}
