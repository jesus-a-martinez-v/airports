package models

case class AirportsPerCountry(country: Country, count: Int)
case class RunwaysPerCountry(country: Country, runwayTypes: Iterable[String])
case class RunwaysIdCount(runwayId: String, count: Int)
case class Report(countriesWithHighestNumberOfAirports: Iterable[AirportsPerCountry],
                 countriesWithLowestNumberOfAirports: Iterable[AirportsPerCountry],
                 runwaysPerCountry: Iterable[RunwaysPerCountry],
                 mostCommonRunwayIdentifications: Iterable[RunwaysIdCount])
