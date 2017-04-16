package data
import java.io.File

import com.github.tototoshi.csv._
import models.{Airport, Country, Runway}

/**
  * Created by jesus on 16/04/17.
  */
object Data {
  private val airportsFileUri = getResourceFileUri("airports_analysis/airports.csv")
  private val countriesFileUri = getResourceFileUri("airports_analysis/countries.csv")
  private val runwaysFileUri = getResourceFileUri("airports_analysis/runways.csv")

  private def getResourceFileUri(filePath: String) = this.getClass.getClassLoader.getResource(filePath).toURI

  private def loadData[T](file: File, mapper: Map[String, String] => T): Iterable[T] = {
    val reader = CSVReader.open(file)
    val results = reader.allWithHeaders().map(mapper)
    reader.close()

    results
  }

  def loadAirports(): Iterable[Airport] = loadData(new File(airportsFileUri), Airport.fromMap)
  def loadCountries(): Iterable[Country] = loadData(new File(countriesFileUri), Country.fromMap)
  def loadRunways(): Iterable[Runway] = loadData(new File(runwaysFileUri), Runway.fromMap)
}
