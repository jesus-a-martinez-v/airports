package http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import services.Summarizer
import spray.json.{JsonParser, pimpAny}

import scala.concurrent.ExecutionContext

/**
  * Created by jesus on 16/04/17.
  */
class SummarizerServiceRoute(summarizer: Summarizer)(implicit executionContext: ExecutionContext) extends BaseServiceRoute {

  def getReport = pathEndOrSingleSlash {
    get {
      complete(summarizer.report())
    }
  }

//  def performQuery = pathPrefix()
}
