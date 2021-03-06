package model

import play.api.libs.json.Json

case class SubContent(
   pid: String,
   id: String,
   caption_id: String,
   number: String,
   part: String,
   hadith: Option[String],
   content: String,
   noTashkeelContent: String,
   cid: String,
   pos: Seq[String])

object SubContent {
  implicit val fmt = Json.format[SubContent]
}

case class Hadith(
  pid: String,
  id: String,
  caption_id: String,
  number: String,
  part: String,
  hadith: Option[String],
  content: String,
  noTashkeelContent: String,
  cid: String,
  authenticity: Option[String],
  pos: Seq[String],
  book: SubContent,
  chapter: SubContent)

object Hadith {
  implicit val fmt = Json.format[Hadith]
}

case class SearchResult(_index: String, _type: String, _id: String, _score: Double, _source: Hadith)

object SearchResult {
  implicit val fmt = Json.format[SearchResult]
}

case class Hits(total: Int, max_score: Option[Double], hits: Seq[SearchResult])

object Hits {
  implicit val fmt = Json.format[Hits]
}

case class ElasticsearchResponse(took: Int, timed_out: Boolean, hits: Hits)

object ElasticsearchResponse {
  implicit val fmt = Json.format[ElasticsearchResponse]
}

case class ShortContent(content: String, noTashkeelContent: String)

object ShortContent {
  implicit val fmt = Json.format[ShortContent]
}

case class HadithResult(index: String, score: Double, content: String, noTashkeelContent: String, authenticity: Option[String], book: ShortContent, chapter: ShortContent)

object HadithResult {
  implicit val fmt = Json.format[HadithResult]

  def apply(index: String, score: Double, hadith: Hadith): HadithResult =
    HadithResult(
      index,
      score,
      hadith.content,
      hadith.noTashkeelContent,
      hadith.authenticity,
      ShortContent(hadith.book.content, hadith.book.noTashkeelContent),
      ShortContent(hadith.chapter.content, hadith.chapter.noTashkeelContent))
}

case class SearchRequest(query: Option[String], offset: Option[Int], limit: Option[Int])

object SearchRequest {
  implicit val fmt = Json.format[SearchRequest]
  val defaultInstance = SearchRequest(query = Some(""), offset = Some(0), limit = Some(10))
}

case class Counter(name: String, count: Int)
