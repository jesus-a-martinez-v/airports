import akka.actor.ActorSystem
import akka.util.Timeout
import data.Data
import models.RunwaysIdCount
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import services.{Summarizer, SummarizerCacheKeeper}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


/**
  * Created by jesus on 17/04/17.
  */
class SummarizerSuite extends FunSuite with ScalaFutures with BeforeAndAfterAll {
  val sparkConfiguration: SparkConf = new SparkConf().setAppName("Airports").setMaster("local")
  implicit val sparkContext: SparkContext = new SparkContext(sparkConfiguration)
  implicit val actorSystem = ActorSystem()
  val cacheKeeper = actorSystem.actorOf(SummarizerCacheKeeper.props(10.minutes))
  implicit val timeout = Timeout(10.seconds)

  private val airports = Data.loadAirports()
  private val countries = Data.loadCountries()
  private val runways = Data.loadRunways()

  val summarizer = new Summarizer(airports, countries, runways, cacheKeeper)

  override def afterAll(): Unit = {
    actorSystem.terminate()
  }

  test("Must calculate correct report") {
    whenReady(summarizer.report()) { report =>
      val countriesWithHighestNumberOfAirports = report.countriesWithHighestNumberOfAirports
        .map(result => (result.country.name, result.count)).toList
      val expected = List(
        ("United States",21501),
        ("Brazil",3839),
        ("Canada",2454),
        ("Australia",1908),
        ("Russia",920),
        ("France",789),
        ("Argentina",713),
        ("Germany",703),
        ("Colombia",700),
        ("Venezuela",592))
      assert(expected == countriesWithHighestNumberOfAirports)

      val countriesWithLowestNumberOfAirports = report.countriesWithLowestNumberOfAirports
        .map(result => (result.country.name, result.count)).toSet
      val expectedSet = Set(
        ("Gibraltar",1),
        ("Cocos (Keeling) Islands",1),
        ("Curaçao",1),
        ("Mayotte",1),
        ("Macau",1),
        ("Monaco",1),
        ("Martinique",1),
        ("Niue",1),
        ("Andorra",1),
        ("Nauru",1))
      assert(expectedSet == countriesWithLowestNumberOfAirports)

      val expectedRunways = List(
        RunwaysIdCount("H1",5566),
        RunwaysIdCount("18",3180),
        RunwaysIdCount("09",2581),
        RunwaysIdCount("17",2320),
        RunwaysIdCount("16",1559),
        RunwaysIdCount("12",1506),
        RunwaysIdCount("14",1469),
        RunwaysIdCount("08",1459),
        RunwaysIdCount("13",1447),
        RunwaysIdCount("15",1399))
      assert(expectedRunways == report.mostCommonRunwayIdentifications.toList)

      val expectedRunwaysPerCountry = Set(
        ("Northern Mariana Islands",3), ("Finland",17), ("Cambodia",5), ("Puerto Rico",8), ("Djibouti",1), ("Portugal",8),
        ("Tunisia",3), ("New Zealand",14), ("Nicaragua",2), ("United Arab Emirates",5), ("Pakistan",10), ("Austria",6),
        ("British Virgin Islands",2), ("Italy",12), ("Algeria",7), ("Laos",3), ("Haiti",1), ("Serbia",5), ("Solomon Islands",14),
        ("Zambia",7), ("Somalia",3), ("Turkmenistan",2), ("Maldives",3), ("Netherlands",7), ("Mozambique",4), ("Senegal",4),
        ("South Sudan",3), ("France",21), ("Guam",2), ("Belgium",7), ("Equatorial Guinea",3), ("Yemen",2), ("Gibraltar",1),
        ("Réunion",1), ("Perú",3), ("Armenia",3), ("Poland",9), ("Guadeloupe",1), ("Malawi",3), ("Macedonia",4), ("Hungary",9),
        ("Croatia",6), ("Norway",10), ("Bermuda",1), ("Israel",3), ("Paraguay",3), ("Mayotte",1), ("Costa Rica",3),
        ("United States",169), ("Wallis and Futuna",2), ("Turkey",7), ("Botswana",3), ("Vanuatu",8), ("Namibia",3),
        ("Greece",3), ("Guatemala",3), ("Congo (Brazzaville)",2), ("Cocos (Keeling) Islands",1), ("Estonia",3), ("Jersey",1),
        ("French Southern Territories",1), ("Libya",5), ("British Indian Ocean Territory",1), ("French Polynesia",4), ("Sint Maarten",2),
        ("Burundi",2), ("American Samoa",3), ("Nepal",3), ("New Caledonia",6), ("Comoros",2), ("Guernsey",2), ("São Tomé and Principe",1),
        ("Norfolk Island",1), ("Niger",3), ("Sri Lanka",2), ("Venezuela",4), ("Angola",2), ("United Kingdom",24), ("Germany",19),
        ("Cuba",2), ("Kenya",4), ("Swaziland",1), ("Montserrat",1), ("Mongolia",5), ("Moldova",2), ("Saint Lucia",1), ("Uganda",3),
        ("Cape Verde",2), ("Ethiopia",3), ("Philippines",9), ("Japan",10), ("Taiwan",4), ("Palestinian Territory",1), ("Canada",96),
        ("Bulgaria",5), ("Uruguay",5), ("El Salvador",1), ("Bahrain",1), ("Falkland Islands",1), ("Lesotho",2), ("Ukraine",7),
        ("Afghanistan",5), ("Nauru",1), ("Palau",3), ("Morocco",4), ("Singapore",2), ("Malaysia",3), ("Saudi Arabia",5),
        ("United States Minor Outlying Islands",2), ("Tajikistan",2), ("Vietnam",3), ("Dominica",1), ("Saint Helena",1), ("Russia",13),
        ("Tonga",4), ("Honduras",5), ("Antigua and Barbuda",1), ("Kazakhstan",6), ("Anguilla",1), ("Western Sahara",1), ("Oman",3),
        ("Marshall Islands",2), ("Sudan",6), ("Saint Martin",1), ("Liberia",1), ("Mauritius",1), ("Belarus",2), ("Lebanon",2), ("Hong Kong",2),
        ("Kuwait",3), ("Barbados",1), ("Timor-Leste",2), ("Côte d'Ivoire",3), ("Isle of Man",1), ("Niue",1), ("Chad",2), ("Seychelles",2),
        ("Denmark",7), ("Eritrea",2), ("Iceland",4), ("Brunei",1), ("Ghana",1), ("Bangladesh",3), ("Albania",5), ("Cameroon",2), ("Colombia",4),
        ("Burkina Faso",2), ("India",19), ("Antarctica",4), ("Syria",6), ("Nigeria",3), ("Togo",2), ("Samoa",1), ("Cyprus",2), ("Spain",8),
        ("Sierra Leone",1), ("Martinique",1), ("Turks and Caicos Islands",2), ("Cook Islands",4), ("Panama",3), ("Bosnia and Herzegovina",3),
        ("Chile",20), ("Aruba",1), ("Lithuania",9), ("Grenada",2), ("Azerbaijan",2), ("Iran",4), ("Argentina",7), ("Sweden",11), ("Burma",3),
        ("Guyana",2), ("Tuvalu",1), ("Georgia",2), ("Benin",2), ("Bolivia",4), ("Romania",5), ("Suriname",1), ("Mali",5), ("Jordan",2),
        ("Indonesia",51), ("Macau",1), ("Luxembourg",2), ("Faroe Islands",1), ("Iraq",7), ("Fiji",2), ("Greenland",6), ("U.S. Virgin Islands",2),
        ("Caribbean Netherlands",1), ("Mauritania",3), ("Kyrgyzstan",5), ("Trinidad and Tobago",1), ("Belize",1), ("Gabon",3), ("Slovenia",5),
        ("Curaçao",1), ("Saint Vincent and the Grenadines",1), ("Cayman Islands",1), ("Papua New Guinea",72), ("Saint Pierre and Miquelon",1),
        ("Egypt",3), ("Latvia",3), ("Ireland",10), ("Mexico",11), ("Guinea",2), ("Christmas Island",1), ("Guinea-Bissau",2), ("French Guiana",2),
        ("Uzbekistan",2), ("South Africa",23), ("Saint Kitts and Nevis",1), ("Thailand",2), ("Dominican Republic",3), ("North Korea",3),
        ("Switzerland",11), ("Kosovo",1), ("Tanzania",5), ("Micronesia",1), ("Ecuador",3), ("Zimbabwe",5), ("China",7), ("Central African Republic",2),
        ("Brazil",34), ("Montenegro",2), ("Slovakia",5), ("Bahamas",3), ("Bhutan",3), ("Jamaica",1), ("Congo (Kinshasa)",6), ("Qatar",2),
        ("Malta",1), ("Kiribati",3), ("Rwanda",3), ("South Korea",10), ("Gambia",1),
        ("Madagascar",4), ("Czech Republic",7), ("Australia",44))

      assert(expectedRunwaysPerCountry == report.runwaysPerCountry.map(result => (result.country.name, result.runwayTypes.size)).toSet)
    }
  }

  test("Must return none when querying a non existent country") {
    whenReady(summarizer.query("NOPE")) { result =>
      assert(result.isEmpty)
    }
  }

  test("Must return the same information when querying a country by name or code") {
    val future1 = summarizer.query("VE", isCode = true)
    val future2 = summarizer.query("Venezuela")

    Thread.sleep(5000)

    whenReady(future1) { resultQueryByCode =>
      whenReady(future2) { resultQueryByName =>
        assert(resultQueryByCode == resultQueryByName)
      }
    }
  }

  test("Must return the valid query result") {
    val future = summarizer.query("VE", isCode = true)
    Thread.sleep(5000)
    whenReady(future) { case Some(queryResult) =>
        assert(queryResult.country.name equalsIgnoreCase "Venezuela")
        assert(queryResult.results.size == 592)
    }
  }
}
