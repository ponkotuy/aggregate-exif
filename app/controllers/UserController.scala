package controllers

import authes.Role.NormalUser
import authes.StackAction
import com.github.tototoshi.play2.json4s.native.Json4s
import javax.inject.{Inject, Singleton}
import models.User
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc._
import queries.CreateUser
import scalikejdbc._

@Singleton
class UserController @Inject()(json4s: Json4s) extends InjectedController {
  import Responses._
  import json4s.implicits._

  implicit def formats = DefaultFormats

  def show() = StackAction(NormalUser) { req =>
    Ok(Extraction.decompose(req.user.minimal))
  }

  def list(public: Boolean) = Action {
    import models.Aliases.u
    val users = if(public) User.findAllBy(sqls.eq(u.public, true))
    else User.findAll()
    Ok(Extraction.decompose(users.map(_.minimal)))
  }

  def showMin(id: Long) = Action {
    User.findById(id).fold(notFound("player")) { user =>
      Ok(Extraction.decompose(user.minimal))
    }
  }

  def createAccount() = Action(json4s.json) { req =>
    req.body.extractOpt[CreateUser].fold(JsonParseError) { user =>
      User.create(user.user())(AutoSession)
      Success
    }
  }

  def public(next: Boolean) = StackAction(NormalUser) { req =>
    val result = User.updateById(req.user.id).withAttributes('public -> next)(req.db)
    if(result > 0) Success else InternalServerError
  }
}
