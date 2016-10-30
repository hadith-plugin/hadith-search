package controllers

import akka.actor.ActorSystem
import javax.inject._

import model.Hadith
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import services.Elasticsearch

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class SearchController(actorSystem: ActorSystem, elasticsearch: Elasticsearch)(implicit exec: ExecutionContext) extends Controller {

  def getQueryAsInt[A](param: String)(implicit request: Request[A]): Option[Int] =
    Try(request.getQueryString(param).map(Integer.parseInt)).toOption.getOrElse(None)

  def search = Action.async { implicit request =>
    val query = request.getQueryString("q").getOrElse("")
    val limit = Math.abs(getQueryAsInt("limit").getOrElse(10))
    val offset = Math.abs(getQueryAsInt("offset").getOrElse(0))
    val json = Json.parse(
      """
        |{
        |  "index": "al-bokhari",
        |  "score": 0.9635,
        |  "content": "   حَدَّثَنَا يَحْيَى بْنُ سُلَيْمَانَ، قَالَ   [ص: 169]  : حَدَّثَنِي ابْنُ وَهْبٍ، قَالَ: حَدَّثَنِي عُمَرُ هُوَ ابْنُ مُحَمَّدٍ، عَنْ سَالِمٍ، عَنْ أَبِيهِ، قَالَ:  وَعَدَ النَّبِيَّ صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ جِبْرِيلُ، فَرَاثَ عَلَيْهِ، حَتَّى اشْتَدَّ عَلَى النَّبِيِّ صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ، فَخَرَجَ النَّبِيُّ صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ فَلَقِيَهُ، فَشَكَا إِلَيْهِ مَا وَجَدَ، فَقَالَ لَهُ: إِنَّا لاَ نَدْخُلُ بَيْتًا فِيهِ صُورَةٌ وَلاَ كَلْبٌ    [تعليق مصطفى البغا]  5615 (5/2222) -[  ش (فراث) أبطأ في النزول. (اشتد) ثقل عليه تأخر نزوله وأحزنه ذلك] [ر 3055] ",
        |  "noTashkeelContent": "   حدثنا يحيى بن سليمان، قال   [ص: 169]  : حدثني ابن وهب، قال: حدثني عمر هو ابن محمد، عن سالم، عن أبيه، قال:  وعد النبي صلى الله عليه وسلم جبريل، فراث عليه، حتى اشتد على النبي صلى الله عليه وسلم، فخرج النبي صلى الله عليه وسلم فلقيه، فشكا إليه ما وجد، فقال له: إنا لا ندخل بيتا فيه صورة ولا كلب    [تعليق مصطفى البغا]  5615 (5/2222) -[  ش (فراث) أبطأ في النزول. (اشتد) ثقل عليه تأخر نزوله وأحزنه ذلك] [ر 3055] ",
        |  "book": {
        |    "content": "« وَكُنْتُ أَغْتَسِلُ أَنَا وَالنَّبِيُّ صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ مِنْ إِنَاءٍ وَاحِدٍ»",
        |    "noTashkeelContent": "« وكنت أغتسل أنا والنبي صلى الله عليه وسلم من إناء واحد»"
        |  },
        |  "chapter": {
        |    "content": " بَابُ لاَ تَدْخُلُ المَلاَئِكَةُ بَيْتًا فِيهِ صُورَةٌ ",
        |    "noTashkeelContent": " باب لا تدخل الملائكة بيتا فيه صورة "
        |  }
        |}
      """.stripMargin)

//    Ok(JsArray(Seq.fill(limit)(json)))
    elasticsearch.search("bokhari", query, offset, limit).map(result => Ok(Json.toJson(result)))
  }

  def add(index: String) = Action.async { implicit request =>
     request.body.asJson.fold(Future.successful(BadRequest("Invalid payload format")))(json =>
      json.validate[Hadith].fold(
        errors =>
          Future.successful(BadRequest(s"Invalid Json, $errors")),
        hadith =>
          elasticsearch.indexHadith(index, hadith).map(result => Ok("Success")))
     )
  }
}
