name := "airports"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  // Scala test kit.
  "org.scalatest" % "scalatest_2.11" % "3.0.0-M16-SNAP6",

  // Spark and Spark SQL dependencies
  "org.apache.spark" %% "spark-core" % "2.1.0",
  "org.apache.spark" %% "spark-sql" % "2.1.0",

  // Akka HTTP dependencies
  "com.typesafe.akka" %% "akka-http-core" % "2.4.10",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.10",
  "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % "2.4.10",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.10" % "test",

  // CSV library dependency
  "com.github.tototoshi" %% "scala-csv" % "1.3.4",

  // Swagger support
  "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.7.2")