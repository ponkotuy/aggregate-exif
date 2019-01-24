package controllers

import authes.StackAction
import com.github.tototoshi.play2.json4s.native.Json4s
import com.ponkotuy.queries.LoginEmail
import javax.inject.Inject
import models.User
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Extraction}
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import play.api.mvc.InjectedController
import scalikejdbc._

import scala.concurrent.ExecutionContext

class SessionController @Inject()(_ec: ExecutionContext, json4s: Json4s) extends InjectedController {
  import Responses._
  import SessionController._
  import json4s.implicits._

  implicit val ec = _ec
  implicit def formats = DefaultFormats ++ JodaTimeSerializers.all

  def login() = Action(json4s.json) { implicit req =>
    DB localTx { implicit db =>
      val result = for {
        auth <- req.body.extractOpt[LoginEmail]
        user <- authenticate(auth)
      } yield user
      result.fold(Forbidden("Login failed.")){ user =>
        Ok(Extraction.decompose(user)).withCookies(StackAction.genSession(user))
      }
    }
  }

  def logout() = Action { implicit req =>
    Success
  }
}

object SessionController {
  import models.Aliases.u
  val bcrypt = new BCryptPasswordEncoder(10)
  def authenticate(le: LoginEmail)(implicit session: DBSession): Option[User] = {
    User.findBy(sqls.eq(u.email, le.email)).filter { user =>
      bcrypt.matches(le.password, user.password)
    }
  }
}
