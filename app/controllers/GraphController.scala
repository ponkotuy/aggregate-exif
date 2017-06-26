package controllers

import javax.inject._

import com.github.tototoshi.play2.json4s.native.Json4s
import models.Image
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller}
import scalikejdbc._

@Singleton
class GraphController @Inject()(json4s: Json4s) extends Controller {
  import json4s._
  import models.Aliases.{i, cond}

  implicit def formats = DefaultFormats

  def iso(userId: Long) = Action{
    val counts = Image.groupByCount(sqls.eq(i.userId, userId), cond.iso)(implicitly[TypeBinder[Int]])(AutoSession)
    val result = counts.map { case (iso, count) => IsoElement(iso, count) }
    Ok(Extraction.decompose(result))
  }
}

case class IsoElement(iso: Int, count: Int)
