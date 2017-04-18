package http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import http.routes.SummarizerServiceRoute

import scala.concurrent.ExecutionContext
/**
  * Created by jesus on 16/04/17.
  */
class HttpService(summarizerServiceRoute: SummarizerServiceRoute)(implicit ec: ExecutionContext) {
  val routes: Route = pathPrefix("v1") {
    summarizerServiceRoute.routes
  }
}
