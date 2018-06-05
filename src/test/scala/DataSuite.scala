import data.Data
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures

class DataSuite extends FunSuite with ScalaFutures {

  private val ExpectedNumberOfAirports = 46505
  private val ExpectedNumberOfCountries = 247
  private val ExpectedNumberOfRunways = 39536

  test("Must load all the airports") {
    assert(Data.loadAirports().size == ExpectedNumberOfAirports)
  }

  test("Must load all the countries") {
    assert(Data.loadCountries().size == ExpectedNumberOfCountries)
  }

  test("Must load all the runways") {
    assert(Data.loadRunways().size == ExpectedNumberOfRunways)
  }

}
