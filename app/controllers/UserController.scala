package controllers

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.google.inject.Inject
import io.circe.generic.auto._
import io.circe.syntax._
import jp.t2v.lab.play2.auth.AuthElement
import models.User
import play.api.libs.circe.Circe
import play.api.mvc._
import queries.CreateUser
import scalikejdbc._

class UserController @Inject()() extends Controller with AuthElement with AuthConfigImpl with Circe {
  import Responses._


  def show() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    Ok((loggedIn: User).asJson)
  }

  def showMin(id: Long) = Action {
    User.findById(id).fold(notFound("player")) { user =>
      Ok(user.minimal.asJson)
    }
  }

  def createAccount() = Action(circe.tolerantJson[CreateUser]) { req =>
    User.create(req.body.user())(AutoSession)
    Success
  }
}
