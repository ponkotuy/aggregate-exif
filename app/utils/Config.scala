package utils

import com.amazonaws.regions.Regions
import play.api.Configuration

class Config(orig: Configuration) {
  lazy val amazon = orig.getConfig("amazon").map(new AmazonConfig(_))
  lazy val mail: Option[String] = orig.getString("management.mail")
}

class AmazonConfig(config: Configuration) {
  lazy val regionRaw: Option[String] = config.getString("region")
  lazy val region: Option[Regions] = regionRaw.map(Regions.fromName)
}
