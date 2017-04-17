package utils

import com.typesafe.config.ConfigFactory

/**
  * Created by jesus on 17/04/17.
  */
trait Configuration {
  //Load configuration information from application.conf into memory
  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")

  //Server parameters.
  val httpHost: String = httpConfig.getString("interface")
  val httpPort: Int = httpConfig.getInt("port")
}
