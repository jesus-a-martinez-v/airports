package models

/**
  * Created by jesus on 16/04/17.
  */
case class QueryResult(country: Country,
                       results: Seq[(Airport, Iterable[Runway])])
