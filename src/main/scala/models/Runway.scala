package models

import utils.Utils

/**
  * Created by jesus on 16/04/17.
  */
case class Runway(id: Long,
                  airportReference: Long,
                  airportIdentifier: String,
                  length: Double,
                  width: Double,
                  surface: String,
                  lighted: Boolean,
                  closed: Boolean,
                  leIdent: Option[String] = None,
                  leLatitude: Option[Double] = None,
                  leLongitude: Option[Double] = None,
                  leElevation: Option[Double] = None,
                  leHeading: Option[Double] = None,
                  leDisplacedThreshold: Option[Double] = None,
                  heIdent: Option[String] = None,
                  heLatitude: Option[Double] = None,
                  heLongitude: Option[Double] = None,
                  heElevation: Option[Double] = None,
                  heHeading: Option[Double] = None,
                  heDisplacedThreshold: Option[Double] = None) {
  require(airportIdentifier.nonEmpty, "airportIdentifier is empty")
  require(surface.nonEmpty, "surface is empty")
  require(leIdent.forall(_.nonEmpty), "leIdent is empty")
  require(heIdent.forall(_.nonEmpty), "heIdent is empty")
}

case object Runway {
  def fromMap(inputMap: Map[String, String]): Runway = {
    def optionByString(string: String) = Utils.optionBy[String](string, _.nonEmpty)
    def optionByDouble(string: String) = Utils.optionBy[String, Double](string, _.nonEmpty, _.toDouble)

    Runway(
      id = inputMap("id").toLong,
      airportReference = inputMap("airport_ref").toLong,
      airportIdentifier = inputMap("airport_ident"),
      length = inputMap("length_ft").toDouble,
      width = inputMap("width_ft").toDouble,
      surface = inputMap("surface"),
      lighted = inputMap("lighted") == "1",
      closed = inputMap("closed") == "1",
      leIdent = optionByString(inputMap("le_ident")),
      leLatitude = optionByDouble(inputMap("le_latitude_deg")),
      leLongitude = optionByDouble(inputMap("le_longitude_deg")),
      leElevation = optionByDouble(inputMap("le_elevation_ft")),
      leHeading = optionByDouble(inputMap("le_heading_degT")),
      leDisplacedThreshold = optionByDouble(inputMap("le_displaced_threshold_ft")),
      heIdent = optionByString(inputMap("he_ident")),
      heLatitude = optionByDouble(inputMap("he_latitude_deg")),
      heLongitude = optionByDouble(inputMap("he_longitude_deg")),
      heElevation = optionByDouble(inputMap("he_elevation_ft")),
      heHeading = optionByDouble(inputMap("he_heading_degT")),
      heDisplacedThreshold = optionByDouble(inputMap("he_displaced_threshold_ft")))
  }
}