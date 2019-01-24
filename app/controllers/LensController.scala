package controllers

import com.github.tototoshi.play2.json4s.native.Json4s
import javax.inject.{Inject, Singleton}
import models.Lens
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.InjectedController

@Singleton
class LensController @Inject()(json4s: Json4s) extends InjectedController {
  import json4s.implicits._
  implicit val formats = DefaultFormats

  def get(id: Long) = Action {
    val lens = Lens.findById(id)
    Ok(Extraction.decompose(lens))
  }
}
