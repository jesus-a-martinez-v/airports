import java.io.File

import scala.io.Source
import com.github.tototoshi.csv._

/**
  * Created by jesus on 16/04/17.
  */
object Main extends App {

  val file = new File(this.getClass.getClassLoader.getResource("airports_analysis/airports.csv").toURI)
  val reader = CSVReader.open(file)
  var airportTypes: Set[String] = Set()

  for(line <- reader.allWithHeaders().take(2)) {
    println(s"line: $line")
  }

  println(airportTypes)
}
