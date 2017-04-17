package utils

import models._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * Defines the JSON mappers used throughout the app.
  */
trait Protocol extends DefaultJsonProtocol {
  implicit val airportFormat: RootJsonFormat[Airport] = jsonFormat18(Airport.apply)
  implicit val countryFormat: RootJsonFormat[Country] = jsonFormat6(Country.apply)
  implicit val runwayFormat: RootJsonFormat[Runway] = jsonFormat20(Runway.apply)
  implicit val reportFormat: RootJsonFormat[Report] = jsonFormat4(Report.apply)
  implicit val queryResultFormat: RootJsonFormat[QueryResult] = jsonFormat2(QueryResult.apply)
}
