package http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import http.routes.SummarizerServiceRoute
import utils.CorsSupport

import scala.concurrent.ExecutionContext

class HttpService(summarizerServiceRoute: SummarizerServiceRoute)(implicit ec: ExecutionContext) extends CorsSupport {

  //Support for Swagger UI.
  private def assets = pathPrefix("swagger") {
    getFromResourceDirectory("swagger") ~
      pathEndOrSingleSlash {
        get {
          redirect("index.html", StatusCodes.PermanentRedirect)
        }
      }
  }

  val routes: Route = corsHandler {
    assets ~ pathPrefix("v1") {
      summarizerServiceRoute.routes
    }
  }
}
