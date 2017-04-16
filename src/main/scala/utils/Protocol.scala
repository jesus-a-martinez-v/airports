package utils

import models._
import spray.json.DefaultJsonProtocol
/**
  * Created by jesus on 16/04/17.
  */
trait Protocol extends DefaultJsonProtocol {
  implicit val airportFormat = jsonFormat18(Airport.apply)
  implicit val countryFormat = jsonFormat6(Country.apply)
  implicit val runwayFormat = jsonFormat20(Runway.apply)
  implicit val reportFormat = jsonFormat4(Report.apply)
  implicit val queryResultFormat = jsonFormat2(QueryResult.apply)
}
