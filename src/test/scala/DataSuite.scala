import data.Data
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures

/**
  * Created by jesus on 17/04/17.
  */
class DataSuite extends FunSuite with ScalaFutures {

  test("Must load all the airports") {
    assert(Data.loadAirports().size == 46505)
  }

  test("Must load all the countries") {
    assert(Data.loadCountries().size == 247)
  }

  test("Must load all the runways") {
    assert(Data.loadRunways().size == 39536)
  }

}
