package controllers

import play.api.data.Form
import play.api.mvc.{Result, Results}

object Responses extends Results{
  val Success = Ok("Success")
  def notFound(name: String) = Results.NotFound(s"${name} not found.")
  def badRequest[A](form: Form[_]): Result = BadRequest(form.errors.mkString("\n"))
  def JsonParseError = BadRequest("JSON Parse Error")
}
