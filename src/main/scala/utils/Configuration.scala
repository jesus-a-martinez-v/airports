package utils

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.FiniteDuration

/**
  * Created by jesus on 17/04/17.
  */
trait Configuration {
  //Load configuration information from application.conf into memory
  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  private val cacheConfig = config.getConfig("cache")

  //Server parameters.
  val httpHost: String = httpConfig.getString("interface")
  val httpPort: Int = httpConfig.getInt("port")
  println(s"DURATION: ${cacheConfig.getDuration("time-to-live", TimeUnit.SECONDS)}")
  val frequency: FiniteDuration = FiniteDuration(cacheConfig.getDuration("time-to-live", TimeUnit.SECONDS), TimeUnit.SECONDS)
}