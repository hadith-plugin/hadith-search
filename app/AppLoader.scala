import com.typesafe.config.ConfigFactory
import controllers.Assets
import controllers.HomeController
import controllers.SearchController
import play.api._
import play.api.ApplicationLoader.Context
import router.Routes
import play.api.libs.ws.ahc.AhcWSClient
import services.ElasticSearchWSC
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class AppLoader extends ApplicationLoader {
  def load(context: Context) = {
    new Components(context).application
  }
}

class Components(context: Context) extends BuiltInComponentsFromContext(context) {
  val ws = AhcWSClient()
  val conf = ConfigFactory.load()
  val elasticsearch = new ElasticSearchWSC(ws, conf.getConfig("elasticsearch"))
  elasticsearch.buildIndex()

  lazy val router = new Routes(
    httpErrorHandler,
    new HomeController(),
    new SearchController(actorSystem, elasticsearch),
    new Assets(httpErrorHandler))
}