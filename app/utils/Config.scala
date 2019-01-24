package utils

import com.amazonaws.regions.Regions
import play.api.Configuration

class Config(orig: Configuration) {
  lazy val amazon = orig.getOptional[Configuration]("amazon").map(new AmazonConfig(_))
  lazy val mail: Option[String] = orig.getOptional[String]("management.mail")
}

class AmazonConfig(config: Configuration) {
  lazy val regionRaw: Option[String] = config.getOptional[String]("region")
  lazy val region: Option[Regions] = regionRaw.map(Regions.fromName)
}
