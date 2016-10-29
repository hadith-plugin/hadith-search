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
  pos: Seq[String],
  book: SubContent,
  chapter: SubContent)

object Hadith {
  implicit val fmt = Json.format[Hadith]
}
