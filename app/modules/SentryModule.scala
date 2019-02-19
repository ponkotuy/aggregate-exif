package modules

import java.net.InetAddress

import com.google.inject.AbstractModule
import io.sentry.Sentry
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment, Logger}
import utils.Config

class SentryModule extends AbstractModule {
  override def configure(): Unit = bind(classOf[SentryService]).asEagerSingleton()
}

@Singleton
class SentryService @Inject()(_conf: Configuration, env: Environment) {
  val logger = Logger(this.getClass)
  val conf = new Config(_conf)
  val Packages = "authes" ::
      "com.ponkotuy" ::
      "controllers" ::
      "modules" ::
      "models" ::
      "queries" ::
      "responses" ::
      "services" ::
      "utils" :: Nil

  conf.sentry.flatMap(_.dsn).foreach { dsn =>
    val packages = Packages.mkString(",")
    val params = Map(
      "environment" -> env.mode.toString,
      "stacktrace.app.packages" -> packages,
      "servername" -> InetAddress.getLocalHost.getHostAddress
    )
    val full = s"${dsn}?${params.map { case (k, v) => s"${k}=${v}" }.mkString("&&")}"
    Sentry.init(full)
    logger.info("Succeed Initialize of Sentry")
  }
}
