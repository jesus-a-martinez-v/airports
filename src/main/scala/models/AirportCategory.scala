package models

/**
  * Created by jesus on 16/04/17.
  */
trait AirportCategory

case object Heliport extends AirportCategory
case object SmallAirport extends AirportCategory
case object MediumAirport extends AirportCategory
case object LargeAirport extends AirportCategory
case object SeaplaneBase extends AirportCategory
case object Balloonport extends AirportCategory
case object Closed extends AirportCategory
case object Unknown extends AirportCategory

object AirportCategory {
  def apply(airportType: String): AirportCategory = airportType.toLowerCase() match {
    case "balloonport" => Balloonport
    case "close" => Closed
    case "large_airport" => LargeAirport
    case "medium_airport" => MediumAirport
    case "small_airport" => SmallAirport
    case "seaplane_base" => SeaplaneBase
    case "heliport" => Heliport
    case _ => Unknown
  }
}