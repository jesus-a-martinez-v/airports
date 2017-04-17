package services

import akka.actor.{Actor, Props}
import models.{QueryResult, Report}
import services.SummarizerCacheKeeper._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

/**
  * Created by jesus on 17/04/17.
  */
class SummarizerCacheKeeper(frequency: FiniteDuration)(implicit executionContext: ExecutionContext) extends Actor {
  private var report: Option[Report] = None
  private var queriesPerCountry: Map[String, QueryResult] = Map()

  /**
    * Filters out the oldest elements in cache every `frequency` units of time.
    * We keep the reference so we can cancel it when this actor dies/stops.
    */
  private val scheduler = context.system.scheduler.schedule(
    frequency,
    frequency,
    context.self,
    Clean)

  /**
    * Cancels the scheduled clean task after the actor has stopped.
    */
  override def postStop(): Unit = {
    if (!scheduler.isCancelled) {
      scheduler.cancel()
    }
    super.postStop()
  }

  override def receive: Receive = {
    case SaveReport(newReport) =>
      report = Some(newReport)
    case SaveQueryForCountry(countryCode, queryResult) =>
      queriesPerCountry += (countryCode -> queryResult)
    case GetReport =>
      sender() ! ReportOperationResult(report)
    case GetQueryForCountry(countryCode) =>
      sender() ! QueryOperationResult(queriesPerCountry.get(countryCode))
    case Clean =>
      report = None
      queriesPerCountry = Map.empty
  }
}

object SummarizerCacheKeeper {

  // Inbound messages
  case class SaveReport(report: Report)

  case class SaveQueryForCountry(countryCode: String, queryResult: QueryResult)

  case object GetReport

  case class GetQueryForCountry(countryCode: String)

  case object Clean

  // Outbound messages
  case class ReportOperationResult(result: Option[Report])
  case class QueryOperationResult(result: Option[QueryResult])

  // Factory method.
  def props(frequency: FiniteDuration)(implicit executionContext: ExecutionContext) = Props(new SummarizerCacheKeeper(frequency))
}
