package controllers

import akka.actor.ActorSystem
import javax.inject._

import model.{Hadith, SearchRequest}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import services.Elasticsearch

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class SearchController(actorSystem: ActorSystem, elasticsearch: Elasticsearch)(implicit exec: ExecutionContext) extends Controller {

  val log = Logger(this.getClass)

  private[this] def getQueryAsInt[A](param: String)(implicit request: Request[A]): Option[Int] =
    Try(request.getQueryString(param).map(Integer.parseInt)).toOption.getOrElse(None)

  private[this] def readURLParameters[A](implicit request: Request[A]): SearchRequest = SearchRequest(
    query = request.getQueryString("q"),
    limit = getQueryAsInt("limit").map(Math.abs),
    offset = getQueryAsInt("offset").map(Math.abs))

  private[this] def readBodyJson(js: JsValue): SearchRequest =
    js.validate[SearchRequest].get

  private[this] def getSearchQuery(implicit request: Request[AnyContent]): SearchRequest =
  request.body.asJson.fold(readURLParameters)(readBodyJson)

  def search = Action.async { implicit request =>
    val searchRequest = getSearchQuery
    val query = searchRequest.query.getOrElse("")
    val limit = searchRequest.limit.getOrElse(10)
    val offset = searchRequest.offset.getOrElse(0)
    elasticsearch.search("bokhari", query, offset, limit).map(result => Ok(Json.toJson(result)))
  }

  def add(index: String) = Action.async { implicit request =>
     request.body.asJson.fold(Future.successful(BadRequest("Invalid payload format")))(json =>
      json.validate[Hadith].fold(
        invalid = { errors =>
          log.info(s"[add] validation faild; errors: $errors")
          Future.successful(BadRequest(s"Invalid Json, $errors"))
        },
        valid = { hadith =>
          log.info(s"[add] validation passed")
          elasticsearch.indexHadith(index, hadith).map(result => result.status match {
            case 200 | 201 =>
              Ok("Success")
            case status =>
              InternalServerError(s"Elasticsearch response: $result")
          })
        })
     )
  }
}
