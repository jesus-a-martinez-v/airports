package models

/**
  * Created by jesus on 16/04/17.
  */
case class Report(countriesWithHighestNumberOfAirports: Iterable[(Country, Int)],
                 countriesWithLowestNumberOfAirports: Iterable[(Country, Int)],
                 runwaysPerCountry: Iterable[(Country, Iterable[String])],
                 mostCommonRunwayIdentifications: Iterable[(String, Int)])
