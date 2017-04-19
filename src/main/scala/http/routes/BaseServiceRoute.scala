package http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import spray.json.JsonWriter
import utils.Protocol

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * This trait provides all the necessary JSON mappers as well as a couple of methods for completing requests in a more
  * elegant and concise way.
  */
trait BaseServiceRoute extends Protocol with SprayJsonSupport {

  protected def completeGracefully[T](operation: => Future[T])(cases: PartialFunction[Try[T], StandardRoute]): Route = {
    onComplete(operation)(cases orElse {
      case Failure(f) => complete(StatusCodes.InternalServerError, s"An error has occurred: ${f.getMessage}.")
    })
  }

  protected def completeSimple[T](operation: => Future[T])(implicit enc: JsonWriter[T]): Route = {
    onComplete(operation) {
      case Success(result) => complete(enc.write(result))
      case Failure(f) => complete(StatusCodes.InternalServerError, s"An error has occurred: ${f.getMessage}.")
    }
  }
}
