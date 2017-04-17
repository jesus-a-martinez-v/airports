package services

import models._
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
  * Created by jesus on 16/04/17.
  */
class Summarizer(airports: Seq[Airport], countries: Seq[Country], runways: Seq[Runway])
                (implicit sparkContext: SparkContext) {

  private val airportsRdd = sparkContext.parallelize(airports)
  private val countriesRdd = sparkContext.parallelize(countries)
  private val runwaysRdd = sparkContext.parallelize(runways)

  def query(countryReference: String, isCode: Boolean = false): Option[QueryResult] = {
    // Look for the country
    val maybeCountry = countriesRdd
      .filter(c => if (isCode) c.code == countryReference else c.name == countryReference)
      .collect()
      .headOption

    maybeCountry map { country =>
      val airportsAndIds: RDD[(String, Airport)] = airportsRdd.map(airport => (airport.identifier, airport))
      val runwaysPerAirport: RDD[(String, Iterable[Runway])] = runwaysRdd.map(runway => (runway.airportIdentifier, runway)).groupByKey()
      val airportsAndRunways: RDD[(Airport, Iterable[Runway])] = airportsAndIds.leftOuterJoin(runwaysPerAirport).map(_._2 match {
        case (airport, Some(rws)) => (airport, rws)
        case (airport, None) => (airport, Iterable.empty)
      })

      QueryResult(country = country, results = airportsAndRunways.filter(_._1.isoCountry == country.code).collect().toSeq)
    }
  }

  def report(): Report = {
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
      .mapValues(_.flatten.collect {case rw if rw.surface.isDefined => rw.surface.get} toSet).collect()

    //////////////////////////////////////////////////////////////////////////////////////

    val top10RunwaysIds: Array[(String, Int)] = runwaysRdd.collect {
      case rw if rw.leIdent.isDefined => (rw.leIdent.get, 1)
    } reduceByKey (_ + _) sortBy (_._2, ascending = false) take 10

    Report(countriesWithHighestNumberOfAirports = topTen,
           countriesWithLowestNumberOfAirports = bottomTen,
           runwaysPerCountry = runwaysPerCountry,
           mostCommonRunwayIdentifications = top10RunwaysIds)
  }
}
