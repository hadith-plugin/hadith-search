package services

import com.typesafe.config.Config
import model.{ElasticsearchResponse, Hadith, HadithResult}
import play.api.Logger
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import play.api.libs.ws.{WSAuthScheme, WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

trait Elasticsearch {
  def indexHadith(index: String, hadith: Hadith, idOpt: Option[String] = None)(implicit ex: ExecutionContext): Future[WSResponse]

  def search(index: String, query: String, offset: Int, limit: Int)(implicit ex: ExecutionContext): Future[Seq[HadithResult]]

  def buildIndex(): Future[Boolean]
}

class ElasticSearchWSC(ws: WSClient, conf: Config) extends Elasticsearch {
  val log = Logger(this.getClass)

  val url = conf.getString("url")
  val user = conf.getString("user")
  val password = conf.getString("password")
  println(s"Elasticsearch URL: $url")
  println(s"user: $user, password: $password")

  override def indexHadith(index: String, hadith: Hadith, idOpt: Option[String] = None)(implicit ex: ExecutionContext): Future[WSResponse] =
    idOpt.fold(request(s"/$index/hadith").post(Json.toJson(hadith)))(id => request(s"/$index/hadith/$id").put(Json.toJson(hadith)))

  override def search(index: String, query: String, offset: Int, limit: Int)(implicit ex: ExecutionContext): Future[Seq[HadithResult]] = {
    val route = s"/$index/hadith/_search?size=$limit&from=$offset"
    val searchRequest = request(route).post(makeQuery(query))
    searchRequest.map(res => res.json.validate[ElasticsearchResponse] match {
      case JsSuccess(result, _) =>
      result.hits.hits.map { hit =>
        HadithResult(index, hit._score, hit._source)
      }
      case JsError(errors) =>
      println("--------------------------------")
      println(res)
      println(errors)
      println("--------------------------------")
      Seq.empty
    })
  }

  override def buildIndex(): Future[Boolean] = Future.successful(true)

  private[this] def request(path: String) = ws.url(s"$url$path").withAuth(user, password, WSAuthScheme.BASIC)

  private[this] def makeQuery(query: String): JsObject = Json.obj(
    "query" -> Json.obj(
      "bool" -> Json.obj(
        "must" -> Json.obj(
          "terms" -> Json.obj(
            "content" -> query.split(" ")
          )
        )
      )
    )
  )
}
