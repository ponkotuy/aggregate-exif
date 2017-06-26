package controllers

import authes.AuthConfigImpl
import com.github.tototoshi.play2.json4s.native.Json4s
import com.google.inject.Inject
import com.ponkotuy.queries.LoginEmail
import jp.t2v.lab.play2.auth.LoginLogout
import models.User
import org.json4s.DefaultFormats
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import play.api.mvc.{Action, Controller}
import scalikejdbc._

import scala.concurrent.ExecutionContext

class SessionController @Inject()(_ec: ExecutionContext, json4s: Json4s)
    extends Controller
    with AuthConfigImpl
    with LoginLogout {
  import json4s._
  import SessionController._
  implicit val ec = _ec
  implicit def formats = DefaultFormats

  def login() = Action.async(json) { implicit req =>
    val result = for {
      auth <- req.body.extractOpt[LoginEmail]
      user <- authenticate(auth)(AutoSession)
    } yield user
    result.fold(authenticationFailed(req)) { user => gotoLoginSucceeded(user.id) }
  }

  def logout() = Action.async { implicit req =>
    gotoLogoutSucceeded
  }
}

object SessionController {
  val bcrypt = new BCryptPasswordEncoder(10)
  def authenticate(le: LoginEmail)(implicit session: DBSession): Option[User] = {
    User.findBy(sqls.eq(User.column.email, le.email)).filter { user =>
      bcrypt.matches(le.password, user.password)
    }
  }
}
