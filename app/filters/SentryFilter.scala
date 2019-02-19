package filters

import java.net.InetAddress

import akka.stream.Materializer
import io.sentry.Sentry
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Filter, RequestHeader, Result}
import play.api.{Configuration, Environment, Logger}
import utils.Config

import scala.concurrent.Future

@Singleton
class SentryFilter @Inject()(env: Environment, _conf: Configuration, implicit val mat: Materializer) extends Filter {
  val logger = Logger(this.getClass)
  val conf = new Config(_conf)
  val Packages = "authes" ::
      "com.ponkotuy" ::
      "controllers" ::
      "filters" ::
      "models" ::
      "queries" ::
      "responses" ::
      "services" ::
      "utils" :: Nil

  // 1度だけ実行したいのでlazy val
  lazy val initialize: Unit = {
    conf.sentry.flatMap(_.dsn).foreach { dsn =>
      val packages = Packages.mkString(",")
      val params = Map(
        "environment" -> env.mode.toString,
        "stacktrace.app.packages" -> packages,
        "servername" -> InetAddress.getLocalHost.getHostAddress
      )
      val full = s"${dsn}?${params.map { case (k, v) => s"${k}=${v}" }.mkString("&&")}"
      Sentry.init(full)
      println("Succeed Initialize of Sentry")
    }
  }

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    initialize
    f(rh)
  }
}
