package models

case class QueryResult(country: Country,
                       results: Seq[(Airport, Iterable[Runway])])
