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

  // Turn collections into RDDs.
  private val airportsRdd = sparkContext.parallelize(airports)
  private val countriesRdd = sparkContext.parallelize(countries)
  private val runwaysRdd = sparkContext.parallelize(runways)

  // Implicit timeout for the ask operation.
  private implicit val timeout = Timeout(5.seconds)

  // Group runways of a same airport together
  private def runwaysPerAirport: RDD[(String, Iterable[Runway])] = runwaysRdd.map(runway => (runway.airportIdentifier, runway)).groupByKey()

  private def saveReportInCache(report: Report): Unit = {
    cacheKeeper ! SummarizerCacheKeeper.SaveReport(report)
  }

  private def saveQueryResultInCache(countryCode: String, queryResult: QueryResult): Unit = {
    cacheKeeper ! SummarizerCacheKeeper.SaveQueryForCountry(countryCode, queryResult)
  }

  private def retrieveReportFromCache: Future[Option[Report]] =
    (cacheKeeper ? SummarizerCacheKeeper.GetReport)
      .mapTo[SummarizerCacheKeeper.ReportOperationResult]
      .map(_.result)

  private def retrieveQueryResultFromCache(countryCode: String): Future[Option[QueryResult]] =
    (cacheKeeper ? SummarizerCacheKeeper.GetQueryForCountry(countryCode))
      .mapTo[SummarizerCacheKeeper.QueryOperationResult]
      .map(_.result)

  private def createReport: Report = {
    val countriesAndCode = countriesRdd.map(country => (country.code, country))

    def getCountriesWithHigherAndLowerAmountOfAirports = {
      // Calculate some useful PairRDDs first.
      val airportsCountPerCountry = airportsRdd.map(airport => (airport.isoCountry, 1)).reduceByKey(_ + _)
      val countriesAndAirportCount = countriesAndCode.join(airportsCountPerCountry).map(_._2)

      // Calculate results for the report.
      val topTen = countriesAndAirportCount.sortBy(_._2, ascending = false).take(10)
      val bottomTen = countriesAndAirportCount.sortBy(_._2, ascending = true).take(10)

      (topTen, bottomTen)
    }

    def getTypeOfRunwaysPerCountry = {

      // PART II: Type of runways per country

      // Pair each airport with its identifier.
      val airportsAndIds: RDD[(String, Airport)] = airportsRdd.map(airport => (airport.identifier, airport))
      // Join the two RDDS. Keep only the country code of the airport and their runways.
      val airportsAndRunways: RDD[(String, Iterable[Runway])] = airportsAndIds.join(runwaysPerAirport).map {
        case (_, (airport, airportRunways)) => (airport.isoCountry, airportRunways)
      }

      val runwaysPerCountry = countriesAndCode
        .join(airportsAndRunways)
        .map(_._2)
        .groupByKey()
        .mapValues(_.flatten.collect { // Merge all runways per airport into a long collection
          case runway if runway.surface.isDefined => runway.surface.get
        } toSet) // We don't want duplicates.
        .collect()

      runwaysPerCountry
    }

    def getTopTenRunwaysIdentifications = {
      // PART 3: Top most common runways identifications.
      val topTenRunwaysIds: Array[(String, Int)] = runwaysRdd.collect {
        case rw if rw.leIdent.isDefined => (rw.leIdent.get, 1) // We count each occurrence of each key.
      }
        .reduceByKey(_ + _) // Sum all occurrences by key.
        .sortBy(_._2, ascending = false)
        .take(10) // Take top 10.

      topTenRunwaysIds
    }

    val (topTen, bottomTen) = getCountriesWithHigherAndLowerAmountOfAirports
    val runwaysPerCountry = getTypeOfRunwaysPerCountry
    val topTenRunwaysIds = getTopTenRunwaysIdentifications

    // Finally, build the report.
    Report(countriesWithHighestNumberOfAirports = topTen,
      countriesWithLowestNumberOfAirports = bottomTen,
      runwaysPerCountry = runwaysPerCountry,
      mostCommonRunwayIdentifications = topTenRunwaysIds)
  }

  private def resolveQuery(country: Country): QueryResult = {
    val airportsAndIds: RDD[(String, Airport)] = airportsRdd.map(airport => (airport.identifier, airport))
    val airportsAndRunways: RDD[(Airport, Iterable[Runway])] =
      airportsAndIds
        .leftOuterJoin(runwaysPerAirport)  // We don't want to leave out airports without runways.
        .map(_._2 match {
          case (airport, Some(rws)) => (airport, rws)
          case (airport, None) => (airport, Iterable.empty)
        })
    val airportsAndRunwaysForSpecificCountry =
      airportsAndRunways
        .filter(_._1.isoCountry equalsIgnoreCase country.code)
        .collect()
        .toSeq

    val queryResult = QueryResult(country = country, results = airportsAndRunwaysForSpecificCountry)

    queryResult
  }

  /**
    * For a given country reference, it returns the following information:
    *   - Runways at each airport.
    *   - Airports in that country.
    * @param countryReference Country name or code.
    * @param isCode Flag that indicates the meaning of the countryReference.
    * @return A query result with the information described above if the passed reference is valid. If not, returns None.
    */
  def query(countryReference: String, isCode: Boolean = false): Future[Option[QueryResult]] = {
    // Look for the country
    val maybeCountry = countriesRdd
      .filter { c =>
        if (isCode)
          c.code equalsIgnoreCase countryReference
        else
          c.name equalsIgnoreCase countryReference
      }
      .collect()
      .headOption

    maybeCountry match {
      case Some(country) =>
        // Check in the cache if the result of this query is available
        retrieveQueryResultFromCache(country.code) map {
          // Resolve query, save the result in cache and return the result
          case None =>
            val queryResult = resolveQuery(country)

            saveQueryResultInCache(country.code, queryResult)
            Some(queryResult)
          // Return cached result
          case hit => hit
        }
      case None => Future.successful(None)
    }
  }

  /**
    * Creates a report with the following information:
    * - Top 10 countries with the highest number of airports and their count.
    * - Top 10 countries with the lowest number of airports and their count.
    * - Type of runways per country.
    * - Top 10 most common runway identifications.
    * @return A report with the information stated above.
    */
  def report(): Future[Report] = {
    // Hit the cache and if there's a report, just return it. Otherwise, make it, cache it and then return.
    retrieveReportFromCache map {
      case Some(report) => report
      case None =>
        val report = createReport
        saveReportInCache(report)
        report
    }
  }

  // Creating the report is an expensive operation, so we do it as soon as this object is created in order to serve it
  // faster.
  println("Creating report. Hang on...")
  saveReportInCache(createReport)
  println("Done! Report created and cached successfully.")
}
