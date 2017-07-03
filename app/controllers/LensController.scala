package controllers

import javax.inject.{Inject, Singleton}

import com.github.tototoshi.play2.json4s.native.Json4s
import models.Lens
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller}

@Singleton
class LensController @Inject()(json4s: Json4s) extends Controller {
  import json4s._
  implicit val formats = DefaultFormats

  def get(id: Long) = Action {
    val lens = Lens.findById(id)
    Ok(Extraction.decompose(lens))
  }
}
