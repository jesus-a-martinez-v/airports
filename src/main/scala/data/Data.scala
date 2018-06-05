package data
import java.io.File

import com.github.tototoshi.csv._
import models.{Airport, Country, Runway}

/**
  * Loads data from CSV files located in /resources/airports_analysis
  */
object Data {
  private val AirportsFileUri = getResourceFileUri("airports_analysis/airports.csv")
  private val CountriesFileUri = getResourceFileUri("airports_analysis/countries.csv")
  private val RunwaysFileUri = getResourceFileUri("airports_analysis/runways.csv")

  private def getResourceFileUri(filePath: String) = this.getClass.getClassLoader.getResource(filePath).toURI
  private def loadData[T](file: File, mapper: Map[String, String] => T): Seq[T] = {
    val reader = CSVReader.open(file)
    val results = reader.allWithHeaders().map(mapper)
    reader.close()

    results
  }

  def loadAirports(): Seq[Airport] = loadData(new File(AirportsFileUri), Airport.fromMap)
  def loadCountries(): Seq[Country] = loadData(new File(CountriesFileUri), Country.fromMap)
  def loadRunways(): Seq[Runway] = loadData(new File(RunwaysFileUri), Runway.fromMap)
}
