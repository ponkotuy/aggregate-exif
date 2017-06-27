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
    val counts = Image.groupByCount(sqls.eq(i.userId, userId), cond.iso)(AutoSession, implicitly[TypeBinder[Int]])
    val result = counts.map { case (iso, count) => IsoElement(iso, count) }
    Ok(Extraction.decompose(result))
  }

  def focal(userId: Long) = Action{
    val counts = Image.groupByCount(sqls.eq(i.userId, userId), cond.focal35)(AutoSession, implicitly[TypeBinder[Int]])
    val result = counts.map { case (focal, count) => FocalElement(focal, count)}
    Ok(Extraction.decompose(result))
  }

  def fNumber(userId: Long) = Action{
    val counts = Image.groupByCount(sqls.eq(i.userId, userId), cond.fNumber)(AutoSession, implicitly[TypeBinder[Double]])
    val result = counts.map { case (fNumber, count) => FNumberElement(fNumber, count)}
    Ok(Extraction.decompose(result))
  }

  def exposure(userId: Long) = Action{
    val counts = Image.groupByCount(sqls.eq(i.userId, userId), cond.exposure)(AutoSession, implicitly[TypeBinder[Int]])
    val result = counts.map { case (exposure, count) => ExposureElement(exposure, count)}
    Ok(Extraction.decompose(result))
  }
}

case class IsoElement(iso: Int, count: Int)
case class FocalElement(focal: Int, count: Int)
case class FNumberElement(fNumber: Double, count: Int)
case class ExposureElement(exposure: Int, count: Int)
