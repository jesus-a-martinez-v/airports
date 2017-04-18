package http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import services.Summarizer

import scala.concurrent.ExecutionContext
import scala.util.Success

/**
  * Endpoints to serve reports and queries about countries.
  */
class SummarizerServiceRoute(summarizer: Summarizer)
                            (implicit executionContext: ExecutionContext) extends BaseServiceRoute {

  private def getReport: Route = pathPrefix("report") {
    pathEndOrSingleSlash {
      get {
        completeSimple(summarizer.report())
      }
    }
  }

  private def performQuery: Route = pathPrefix("query") {
    pathPrefix(Segment) { countryReference =>
      pathEndOrSingleSlash {
        parameters('referenceIsCode.as[Boolean] ? true) { referenceIsCode =>
          completeGracefully(summarizer.query(countryReference, referenceIsCode)) {
            case Success(Some(queryResult)) => complete(queryResult)
            case Success(None) => complete(StatusCodes.NotFound)
          }
        }
      }
    }
  }

  val routes: Route = getReport ~ performQuery
}
