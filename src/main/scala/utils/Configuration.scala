package utils

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.FiniteDuration

/**
  * App configurations.
  */
trait Configuration {
  // Load configuration information from application.conf into memory
  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  private val cacheConfig = config.getConfig("cache")

  // Server parameters.
  val httpHost: String = httpConfig.getString("interface")
  val httpPort: Int = httpConfig.getInt("port")
  val frequency: FiniteDuration = FiniteDuration(cacheConfig.getDuration("time-to-live", TimeUnit.SECONDS), TimeUnit.SECONDS)
}
