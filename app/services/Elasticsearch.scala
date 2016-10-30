package services

import com.typesafe.config.Config
import model.Hadith
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait Elasticsearch {
  def indexHadith(index: String, hadith: Hadith)(implicit ex: ExecutionContext): Future[Boolean]

  def search(index: String, query: String, offset: Int, limit: Int)(implicit ex: ExecutionContext): Future[Seq[Hadith]]
}

class ElasticSearchWSC(ws: WSClient, conf: Config) extends Elasticsearch {
  val url = conf.getString("url")

  override def indexHadith(index: String, hadith: Hadith)(implicit ex: ExecutionContext): Future[Boolean] = {
    ws.url(s"$url/$index").post(Json.toJson(hadith)).map(response => response.status == 201)
  }

  override def search(index: String, query: String, offset: Int, limit: Int)(implicit ex: ExecutionContext): Future[Seq[Hadith]] = ???
}
