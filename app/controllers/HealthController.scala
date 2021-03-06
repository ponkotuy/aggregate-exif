package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.InjectedController

@Singleton
class HealthController @Inject()() extends InjectedController {
  def error() = Action {
    println(1 / 0)
    Ok
  }
}
