import model.{FeedArticle, Tag}
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup

case object ContentProcessor {

  val filterTags = "img,a,svg,button,form,figure,dl"
  val filterPhrases = List(
    "share page",
    "related links",
    "related internet links",
    "related topics",
    "more on this story",
    "the BBC is not responsible for the content of external sites",
    "subscribe to our channel"
  )

  def processContent(article: FeedArticle): FeedArticle = {
    val elements = Jsoup.parse(article.content).getElementsByTag("article")
    elements.select(filterTags).remove()

    val content = filterPhrases.foldLeft(elements.text())((a, b) => StringUtils.removeIgnoreCase(a, b))

    article.copy(content = content, tags = processTags(article.link).toSeq)
  }

  def processTags(link: String): Set[String] = {
    link.split("/").filter(StringUtils.isNotBlank(_)).flatMap { item =>
      Tag.tags.flatMap(tag => searchKeywords(item, tag))
    }.toSet
  }

  def searchKeywords(item: String, tag: Tag): Option[String] = {
    tag.keywords.find(k => StringUtils.containsAnyIgnoreCase(item, k)).map(_ => tag.tagName)
  }

}
