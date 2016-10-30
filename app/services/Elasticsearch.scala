package services

import com.typesafe.config.Config
import model.{ElasticsearchResponse, Hadith}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait Elasticsearch {
  def indexHadith(index: String, hadith: Hadith)(implicit ex: ExecutionContext): Future[Boolean]

  def search(index: String, query: String, offset: Int, limit: Int)(implicit ex: ExecutionContext): Future[Seq[Hadith]]
}

class ElasticSearchWSC(ws: WSClient, conf: Config) extends Elasticsearch {
  val url = conf.getString("url")

  override def indexHadith(index: String, hadith: Hadith)(implicit ex: ExecutionContext): Future[Boolean] = {
    ws.url(s"$url/$index/hadith").post(Json.toJson(hadith)).map(response => response.status == 202)
  }

  override def search(index: String, query: String, offset: Int, limit: Int)(implicit ex: ExecutionContext): Future[Seq[Hadith]] =
    ws.url(s"$url/$index/hadith/_search").get().map{x => println(x.json); x}.map(_.json.validate[ElasticsearchResponse] match {
      case JsSuccess(result, _) => result.hits.hits.map(_._source)
      case JsError(errors) =>
        println(errors)
        Seq.empty
    })
}
