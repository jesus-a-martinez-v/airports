import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import data.Data
import http.HttpService
import http.routes.SummarizerServiceRoute
import org.apache.spark.{SparkConf, SparkContext}
import services.{Summarizer, SummarizerCacheKeeper}
import utils.Configuration

import scala.concurrent.ExecutionContext

/**
  * Service initialization and entry point.
  */
object Main extends App with Configuration {
  private implicit val system = ActorSystem()

  // Use the actor system's embedded dispatcher
  private implicit val executor: ExecutionContext = system.dispatcher
  private val log: LoggingAdapter = Logging(system, getClass)
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  // Spawn spark context.
  private val sparkConfiguration: SparkConf = new SparkConf().setAppName("Airports").setMaster("local")
  implicit val sparkContext: SparkContext = new SparkContext(sparkConfiguration)

  // Load data from CSVs
  val airports = Data.loadAirports()
  val countries = Data.loadCountries()
  val runways = Data.loadRunways()

  // Create summarizer cache keeper
  val cacheKeeper = system.actorOf(SummarizerCacheKeeper.props(frequency))

  // Create summarizer service
  val summarizer = new Summarizer(airports, countries, runways, cacheKeeper)

  // Create HTTP services.
  val summarizerServiceRoute = new SummarizerServiceRoute(summarizer)
  val httpService = new HttpService(summarizerServiceRoute)

  // Run server and start serving.
  println(s"Server started. Listening on $httpHost:$httpPort")
  Http().bindAndHandle(httpService.routes, httpHost, httpPort)
}
