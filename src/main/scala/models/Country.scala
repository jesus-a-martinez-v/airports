package models

case class Country(id: Long, code: String, name: String, continent: String, wikipediaLink: String, keywords: Iterable[String] = Iterable.empty) {
  require(code.nonEmpty, "code is empty")
  require(name.nonEmpty, "name is empty")
  require(continent.nonEmpty, "continent is empty")
  require(wikipediaLink.nonEmpty, "wikipediaLink is empty")
  require(keywords.forall(_.nonEmpty), "keywords cannot contain empty strings")
}

object Country {
  def fromMap(inputMap: Map[String, String]): Country = {
    // Parse and assign accordingly
    Country(id = inputMap("id").toLong,
            code = inputMap("code"),
            name = inputMap("name"),
            continent = inputMap("continent"),
            wikipediaLink = inputMap("wikipedia_link"),
            keywords = inputMap("keywords").split(",").filterNot(_.isEmpty))
  }
}