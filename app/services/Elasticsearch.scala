package services

import com.typesafe.config.Config
import model.{ElasticsearchResponse, Hadith, HadithResult}
import play.api.Logger
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait Elasticsearch {
  def indexHadith(index: String, hadith: Hadith)(implicit ex: ExecutionContext): Future[Boolean]

  def search(index: String, query: String, offset: Int, limit: Int)(implicit ex: ExecutionContext): Future[Seq[HadithResult]]

  def buildIndex(): Future[Boolean]
}

class ElasticSearchWSC(ws: WSClient, conf: Config) extends Elasticsearch {
  val url = conf.getString("url")
  val log = Logger(this.getClass)

  override def indexHadith(index: String, hadith: Hadith)(implicit ex: ExecutionContext): Future[Boolean] = {
    ws.url(s"$url/$index/hadith").post(Json.toJson(hadith)).map(response => response.status match {
      case 200 | 201 =>
        log.info("Created")
        true
      case other =>
        log.info("Failed; status: $other")
        false
    })
  }

  override def search(index: String, query: String, offset: Int, limit: Int)(implicit ex: ExecutionContext): Future[Seq[HadithResult]] =
    ws.url(s"$url/$index/hadith/_search").post(makeQuery(query)).map(_.json.validate[ElasticsearchResponse] match {
      case JsSuccess(result, _) =>
        result.hits.hits.map { hit =>
          HadithResult(index, hit._score, hit._source)
        }
      case JsError(errors) =>
        println(errors)
        Seq.empty
    })

  override def buildIndex(): Future[Boolean] = Future.successful(true)

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
