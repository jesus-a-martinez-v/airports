import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation
import akka.stream.ActorMaterializer
import data.Data
import http.HttpService
import http.routes.SummarizerServiceRoute
import http.routes.swagger.SwaggerDocService
import org.apache.spark.{SparkConf, SparkContext}
import services.{Summarizer, SummarizerCacheKeeper}
import utils.Configuration

import scala.concurrent.ExecutionContext

/**
  * Service initialization and entry point.
  */
object Main extends App with Configuration with RouteConcatenation {
  private implicit val system: ActorSystem = ActorSystem()

  // Use the actor system's embedded dispatcher
  private implicit val executor: ExecutionContext = system.dispatcher
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  // Spawn spark context.
  private val sparkConfiguration: SparkConf = new SparkConf().setAppName("Airports").setMaster("local")
  private implicit val sparkContext: SparkContext = new SparkContext(sparkConfiguration)

  // Load data from CSVs
  private val airports = Data.loadAirports()
  private val countries = Data.loadCountries()
  private val runways = Data.loadRunways()

  // Create summarizer cache keeper
  private val cacheKeeper = system.actorOf(SummarizerCacheKeeper.props(frequency))

  // Create summarizer service
  private val summarizer = new Summarizer(airports, countries, runways, cacheKeeper)

  // Create HTTP services.
  private val summarizerServiceRoute = new SummarizerServiceRoute(summarizer)
  private val httpService = new HttpService(summarizerServiceRoute)

  // Create Swagger service
  private val swaggerService = new SwaggerDocService(system, httpHost, httpPort)

  private val allRoutes = httpService.routes ~ swaggerService.routes

  // Run server and start serving.
  println(s"Server started. Listening on $httpHost:$httpPort")
  Http().bindAndHandle(allRoutes, httpHost, httpPort)
}
