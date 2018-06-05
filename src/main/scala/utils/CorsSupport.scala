package utils

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import com.typesafe.config.ConfigFactory

trait CorsSupport {
  lazy val allowedOriginHeader: `Access-Control-Allow-Origin` = {
    val config = ConfigFactory.load()
    val allowedOrigin = config.getString("cors.allowed-origin")
    if (allowedOrigin == "*") {
      `Access-Control-Allow-Origin`.*
    } else {
      `Access-Control-Allow-Origin`(HttpOrigin(allowedOrigin))
    }
  }

  private def addAccessControlHeaders(): Directive0 = {
    mapResponseHeaders { headers =>
      allowedOriginHeader +:
        `Access-Control-Allow-Credentials`(true) +:
        `Access-Control-Allow-Headers`("Token", "Content-Type", "X-Requested-With") +:
        headers
    }
  }

  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(200).withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE, PATCH)))
  }

  def corsHandler(r: Route): Route = addAccessControlHeaders() {
    preflightRequestHandler ~ r
  }
}