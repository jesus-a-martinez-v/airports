package services

import akka.actor.ActorRef
import models._
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by jesus on 16/04/17.
  */
class Summarizer(airports: Seq[Airport], countries: Seq[Country], runways: Seq[Runway], cacheKeeper: ActorRef)
                (implicit sparkContext: SparkContext, executionContext: ExecutionContext) {

  private val airportsRdd = sparkContext.parallelize(airports)
  private val countriesRdd = sparkContext.parallelize(countries)
  private val runwaysRdd = sparkContext.parallelize(runways)

  /**
    * Implicit timeout for the ask operation.
    */
  private implicit val timeout = Timeout(5.seconds)

  private def saveReport(report: Report): Unit = {
    cacheKeeper ! SummarizerCacheKeeper.SaveReport(report)
  }

  private def saveQueryResult(countryCode: String, queryResult: QueryResult): Unit = {
    cacheKeeper ! SummarizerCacheKeeper.SaveQueryForCountry(countryCode, queryResult)
  }

  private def retrieveReport: Future[Option[Report]] =
    (cacheKeeper ? SummarizerCacheKeeper.GetReport)
      .mapTo[SummarizerCacheKeeper.ReportOperationResult]
      .map(_.result)

  private def retrieveQueryForCountry(countryCode: String): Future[Option[QueryResult]] =
    (cacheKeeper ? SummarizerCacheKeeper.GetQueryForCountry(countryCode))
      .mapTo[SummarizerCacheKeeper.QueryOperationResult]
      .map(_.result)

  private def createReport: Report = {
    val countriesAndCode = countriesRdd.map(country => (country.code, country))
    val airportsPerCountry = airportsRdd.map(airport => (airport.isoCountry, 1)).reduceByKey(_ + _)
    val countriesAndAirportCount = countriesAndCode.join(airportsPerCountry).map(_._2)

    val topTen = countriesAndAirportCount.sortBy(_._2, ascending = false).cache().take(10)
    val bottomTen = countriesAndAirportCount.sortBy(_._2, ascending = true).cache().take(10)

    //////////////////////////////////////////////////////////////////////////////////////

    val airportsAndIds: RDD[(String, Airport)] = airportsRdd.map(airport => (airport.identifier, airport))
    val runwaysPerAirport: RDD[(String, Iterable[Runway])] = runwaysRdd.map(runway => (runway.airportIdentifier, runway)).groupByKey()
    val airportsAndRunways: RDD[(String, Iterable[Runway])] = airportsAndIds.join(runwaysPerAirport).map {
      case (_, (airport, rws)) => (airport.isoCountry, rws)
    }

    //////////////////////////////////////////////////////////////////////////////////////

    val runwaysPerCountry = countriesAndCode
      .join(airportsAndRunways)
      .map(_._2)
      .groupByKey()
      .mapValues(_.flatten.collect { case rw if rw.surface.isDefined => rw.surface.get } toSet).collect()

    //////////////////////////////////////////////////////////////////////////////////////

    val top10RunwaysIds: Array[(String, Int)] = runwaysRdd.collect {
      case rw if rw.leIdent.isDefined => (rw.leIdent.get, 1)
    } reduceByKey (_ + _) sortBy(_._2, ascending = false) take 10

    Report(countriesWithHighestNumberOfAirports = topTen,
      countriesWithLowestNumberOfAirports = bottomTen,
      runwaysPerCountry = runwaysPerCountry,
      mostCommonRunwayIdentifications = top10RunwaysIds)
  }

  def query(countryReference: String, isCode: Boolean = false): Future[Option[QueryResult]] = {
    // Look for the country
    val maybeCountry = countriesRdd
      .filter(c => if (isCode) c.code == countryReference else c.name == countryReference)
      .collect()
      .headOption

    maybeCountry match {
      case Some(country) =>
        retrieveQueryForCountry(country.code) map {
          case None =>
            val airportsAndIds: RDD[(String, Airport)] = airportsRdd.map(airport => (airport.identifier, airport))
            val runwaysPerAirport: RDD[(String, Iterable[Runway])] = runwaysRdd.map(runway => (runway.airportIdentifier, runway)).groupByKey()
            val airportsAndRunways: RDD[(Airport, Iterable[Runway])] = airportsAndIds.leftOuterJoin(runwaysPerAirport).map(_._2 match {
              case (airport, Some(rws)) => (airport, rws)
              case (airport, None) => (airport, Iterable.empty)
            })

            val queryResult = QueryResult(country = country, results = airportsAndRunways.filter(_._1.isoCountry == country.code).collect().toSeq)

            saveQueryResult(country.code, queryResult)

            Some(queryResult)
          case hit => hit
        }
      case None => Future.successful(None)
    }
  }

  def report(): Future[Report] = {
    retrieveReport map {
      case Some(report) => report
      case None =>
        val report = createReport
        saveReport(report)
        report
    }
  }

  // Creating the report is an expensive operation, so we do it as soon as this object is created in order to serve it
  // faster.
  saveReport(createReport)
}
