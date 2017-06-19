package controllers

import authes.AuthConfigImpl
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.LoginLogout
import play.api.libs.circe.Circe
import play.api.mvc.{Action, Controller}
import queries.LoginEmail
import scalikejdbc.AutoSession

import scala.concurrent.ExecutionContext

class SessionController @Inject()(_ec: ExecutionContext)
    extends Controller
    with AuthConfigImpl
    with LoginLogout
    with Circe {
  implicit val ec = _ec

  def login() = Action.async(circe.tolerantJson[LoginEmail]) { implicit req =>
    val result = for {
      user <- req.body.authenticate()(AutoSession)
    } yield user
    result.fold(authenticationFailed(req)) { user => gotoLoginSucceeded(user.id) }
  }

  def logout() = Action.async { implicit req =>
    gotoLogoutSucceeded
  }
}
