package models

import utils.Utils

/**
  * Created by jesus on 15/04/17.
  */
case class Airport(id: Long, identifier: String, airportType: String, name: String, latitude: Double, longitude: Double,
                   elevation: Option[Double] = None, continent: String, isoCountry: String, isoRegion: String, scheduledService: Boolean,
                   municipality: Option[String] = None, gpsCode: Option[String] = None, iataCode: Option[String] = None,
                   localCode: Option[String] = None, homeLink: Option[String] = None,
                   wikipediaLink: Option[String] = None, keywords: Iterable[String] = Iterable.empty) {
  require(identifier.nonEmpty, "identifier is empty")
  require(airportType.nonEmpty, "airportType is empty")
  require(name.nonEmpty, "name is empty")
  require(continent.nonEmpty, "continent is empty")
  require(isoCountry.nonEmpty, "isoCountry is empty")
  require(isoRegion.nonEmpty, "isoRegion is empty")
  require(municipality.forall(_.nonEmpty), "municipality is empty")
  require(gpsCode.forall(_.nonEmpty), "gpsCode is empty")
  require(iataCode.forall(_.nonEmpty), "iataCode is empty")
  require(wikipediaLink.forall(_.nonEmpty), "wikipediaLink is empty")
  require(localCode.forall(_.nonEmpty), "localCode is empty")
  require(homeLink.forall(_.nonEmpty), "homeLink is empty")
  require(keywords.forall(_.nonEmpty), "keywords cannot contain empty strings")
}

object Airport {
  def fromMap(inputMap: Map[String, String]): Airport = {
    // Parse and assign accordingly.
    Airport(id = inputMap("id").toLong,
            identifier = inputMap("ident"),
            airportType = inputMap("type"),
            name = inputMap("name"),
            latitude = inputMap("latitude_deg").toDouble,
            longitude = inputMap("longitude_deg").toDouble,
            elevation = Utils.optionBy[String, Double](inputMap("elevation_ft"), _.nonEmpty, _.toDouble),
            continent = inputMap("continent"),
            isoCountry = inputMap("iso_country"),
            isoRegion = inputMap("iso_region"),
            scheduledService = inputMap("scheduled_service").toLowerCase() == "yes",
            municipality = Utils.optionBy[String](inputMap("municipality"), _.nonEmpty),
            gpsCode = Utils.optionBy[String](inputMap("gps_code"), _.nonEmpty),
            iataCode = Utils.optionBy[String](inputMap("iata_code"), _.nonEmpty),
            localCode = Utils.optionBy[String](inputMap("local_code"), _.nonEmpty),
            homeLink = Utils.optionBy[String](inputMap("home_link"), _.nonEmpty),
            wikipediaLink = Utils.optionBy[String](inputMap("wikipedia_link"), _.nonEmpty),
            keywords = inputMap("keywords").split(",").filterNot(_.isEmpty))
  }
}