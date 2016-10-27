package controllers

import akka.actor.ActorSystem
import javax.inject._
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import scala.concurrent.ExecutionContext

@Singleton
class SearchController @Inject() (actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends Controller {

  def search = Action {
    val json = Json.parse(
      """
        |1
      """.stripMargin)

    Ok(JsArray(Seq.fill(10)(json)))
  }


}
