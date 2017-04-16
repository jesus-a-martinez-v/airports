package http.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.ActorMaterializer
import spray.json.JsonWriter
import utils.Protocol

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait BaseServiceRoute extends Protocol with SprayJsonSupport {

  protected def completeGracefully[T](operation: => Future[T])(cases: PartialFunction[Try[T], StandardRoute]) = {
    onComplete(operation)(cases orElse {
      case Failure(f) => complete(StatusCodes.InternalServerError, s"An error has occurred: ${f.getMessage}")
    })
  }

  protected def completeSimple[T](operation: => Future[T])(implicit enc: JsonWriter[T]) = {
    onComplete(operation) {
      case Success(result) => complete(enc.write(result))
      case Failure(f) => complete(StatusCodes.InternalServerError, s"An error has occurred: ${f.getMessage}")
    }
  }
}
