package controllers

import authes.Role.NormalUser
import authes.StackAction
import com.github.tototoshi.play2.json4s.native.Json4s
import javax.inject._
import models.{Camera, Image, Lens, User}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{InjectedController, Request}
import queries.GraphFilter
import scalikejdbc._

import scala.collection.breakOut
import scala.concurrent.ExecutionContext

@Singleton
class GraphController @Inject()(json4s: Json4s, _ec: ExecutionContext) extends InjectedController {
  import json4s.implicits._
  import models.Aliases.{cond, i}

  implicit def ec = _ec
  implicit def formats = DefaultFormats

  def viewable(userId: Long) = Action { implicit req =>
    Ok(auth(userId).toString)
  }

  def focal(userId: Long) =Action { implicit req =>
    val enabled = auth(userId)
    if(enabled) {
      val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
      val counts = Image.groupByCondCount(where, cond.focal35)(AutoSession, implicitly[TypeBinder[Int]])
      val result = counts.map{ case (focal, count) => FocalElement(focal, count) }
      Ok(Extraction.decompose(result))
    } else Forbidden
  }

  def iso(userId: Long) = Action { implicit req =>
    val enabled = auth(userId)
    if(enabled) {
      val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
      val counts = Image.groupByCondCount(where, cond.iso)(AutoSession, implicitly[TypeBinder[Int]])
      val result = counts.map{ case (iso, count) => IsoElement(iso, count) }
      Ok(Extraction.decompose(result))
    } else Forbidden
  }

  def fNumber(userId: Long) = Action { implicit req =>
    val enabled = auth(userId)
    if(enabled) {
      val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
      val counts = Image.groupByCondCount(where, cond.fNumber)(AutoSession, implicitly[TypeBinder[Double]])
      val result = counts.map{ case (fNumber, count) => FNumberElement(fNumber, count) }
      Ok(Extraction.decompose(result))
    } else Forbidden
  }

  def exposure(userId: Long) = Action{ implicit req =>
    val enabled = auth(userId)
    if(enabled) {
      val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
      val counts = Image.groupByCondCount(where, cond.exposure)(AutoSession, implicitly[TypeBinder[Int]])
      val result = counts.map{ case (exposure, count) => ExposureElement(exposure, count) }
      Ok(Extraction.decompose(result))
    } else Forbidden
  }

  def lens(userId: Long) = Action { implicit req =>
    val enabled = auth(userId)
    if(enabled) {
      val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
      val counts = Image.groupByCount(where, i.lensId)(AutoSession, implicitly[TypeBinder[Option[Long]]])
      val lens: Map[Long, String] = Lens.findAll().map{ l => l.id -> l.name }(breakOut)
      val result = counts.flatMap{ case (id, count) => id.map { i => LensElement(i, lens(i), count) } }
      Ok(Extraction.decompose(result))
    } else Forbidden
  }

  def camera(userId: Long) = Action { implicit req =>
    val enabled = auth(userId)
    if(enabled) {
      val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
      val counts = Image.groupByCount(where, i.cameraId)(AutoSession, implicitly[TypeBinder[Long]])
      val cameras: Map[Long, String] = Camera.findAll().map{ c => c.id -> s"${c.name}(${c.maker})" }(breakOut)
      val result = counts.map{ case (id, count) => CameraElement(id, cameras(id), count) }
      Ok(Extraction.decompose(result))
    } else Forbidden
  }

  private def auth[T](userId: Long)(implicit req: Request[T], ec: ExecutionContext): Boolean = {
    User.findById(userId).fold(false) { user =>
      if(user.public) true
      else {
        StackAction.auth(NormalUser)(req).fold(false) { req => req.user.id == userId }
      }
    }
  }
}

case class IsoElement(iso: Int, count: Int)
case class FocalElement(focal: Int, count: Int)
case class FNumberElement(fNumber: Double, count: Int)
case class ExposureElement(exposure: Int, count: Int)
case class LensElement(id: Long, lens: String, count: Int)
case class CameraElement(id: Long, camera: String, count: Int)
