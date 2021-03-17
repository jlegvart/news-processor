package config

import com.typesafe.config.ConfigFactory

case object AppConfig {

  val config = ConfigFactory.load("local")

}
