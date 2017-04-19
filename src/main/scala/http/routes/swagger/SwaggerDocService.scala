package http.routes.swagger

import akka.actor.ActorSystem
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.github.swagger.akka._
import com.github.swagger.akka.model.Info
import http.routes.SummarizerServiceRoute
import spray.json.pimpString

import scala.reflect.runtime.{universe => ru}

class SwaggerDocService(system: ActorSystem, hostname: String, port: Int) extends SwaggerHttpService with HasActorSystem {
  override implicit val actorSystem: ActorSystem = system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()

  //Types used to create swagger.json
  override val apiTypes = Seq(ru.typeOf[SummarizerServiceRoute])

  //Address where the JSON will be served.
  override val host = s"$hostname:$port"
  override val info = Info(version = "1.0")
  override val basePath = "/v1"    //the basePath for the API we are exposing

  override val routes: Route = get {
    path("swagger.json") {
      complete(toJsonString(swagger).parseJson.asJsObject)
    }
  }
}