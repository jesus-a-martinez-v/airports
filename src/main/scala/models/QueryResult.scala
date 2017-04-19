package models

case class RunwaysPerAirport(airport: Airport, runways: Iterable[Runway])
case class QueryResult(country: Country,
                       results: Seq[RunwaysPerAirport])
