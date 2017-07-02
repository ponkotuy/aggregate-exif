package controllers

import javax.inject._

import com.github.tototoshi.play2.json4s.native.Json4s
import models.{Camera, Image, Lens}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller}
import queries.GraphFilter
import scalikejdbc._

import scala.collection.breakOut

@Singleton
class GraphController @Inject()(json4s: Json4s) extends Controller {
  import json4s._
  import models.Aliases.{i, cond}

  implicit def formats = DefaultFormats


  def focal(userId: Long) = Action{ req =>
    val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
    val counts = Image.groupByCondCount(where, cond.focal35)(AutoSession, implicitly[TypeBinder[Int]])
    val result = counts.map { case (focal, count) => FocalElement(focal, count)}
    Ok(Extraction.decompose(result))
  }

  def iso(userId: Long) = Action{ req =>
    val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
    val counts = Image.groupByCondCount(where, cond.iso)(AutoSession, implicitly[TypeBinder[Int]])
    val result = counts.map { case (iso, count) => IsoElement(iso, count) }
    Ok(Extraction.decompose(result))
  }

  def fNumber(userId: Long) = Action{ req =>
    val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
    val counts = Image.groupByCondCount(where, cond.fNumber)(AutoSession, implicitly[TypeBinder[Double]])
    val result = counts.map { case (fNumber, count) => FNumberElement(fNumber, count)}
    Ok(Extraction.decompose(result))
  }

  def exposure(userId: Long) = Action{ req =>
    val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
    val counts = Image.groupByCondCount(where, cond.exposure)(AutoSession, implicitly[TypeBinder[Int]])
    val result = counts.map { case (exposure, count) => ExposureElement(exposure, count)}
    Ok(Extraction.decompose(result))
  }

  def lens(userId: Long) = Action{ req =>
    val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
    val counts = Image.groupByCount(where, i.lensId)(AutoSession, implicitly[TypeBinder[Long]])
    val lens: Map[Long, String] = Lens.findAll().map { l => l.id -> l.name }(breakOut)
    val result = counts.map { case (id, count) => LensElement(id, lens(id), count) }
    Ok(Extraction.decompose(result))
  }

  def camera(userId: Long) = Action{ req =>
    val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
    val counts = Image.groupByCount(where, i.cameraId)(AutoSession, implicitly[TypeBinder[Long]])
    val cameras: Map[Long, String] = Camera.findAll().map { c => c.id -> s"${c.name}(${c.maker})" }(breakOut)
    val result = counts.map { case (id, count) => CameraElement(id, cameras(id), count) }
    Ok(Extraction.decompose(result))
  }
}

case class IsoElement(iso: Int, count: Int)
case class FocalElement(focal: Int, count: Int)
case class FNumberElement(fNumber: Double, count: Int)
case class ExposureElement(exposure: Int, count: Int)
case class LensElement(id: Long, lens: String, count: Int)
case class CameraElement(id: Long, camera: String, count: Int)
