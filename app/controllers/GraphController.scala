package controllers

import javax.inject._

import authes.{AuthConfigImpl, SessionContainer}
import com.github.tototoshi.play2.json4s.native.Json4s
import jp.t2v.lab.play2.auth.AuthElement
import models.{Camera, Image, Lens, User}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller, RequestHeader}
import queries.GraphFilter
import scalikejdbc._

import scala.collection.breakOut
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GraphController @Inject()(json4s: Json4s, _ec: ExecutionContext) extends Controller with AuthElement with AuthConfigImpl{
  import json4s._
  import models.Aliases.{cond, i}

  implicit def ec = _ec
  implicit def formats = DefaultFormats

  def viewable(userId: Long) = Action.async{ implicit req =>
    auth(userId).map { b => Ok(b.toString) }
  }

  def focal(userId: Long) =Action.async{ implicit req =>
    auth(userId).map { enabled =>
      if(enabled) {
        val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
        val counts = Image.groupByCondCount(where, cond.focal35)(AutoSession, implicitly[TypeBinder[Int]])
        val result = counts.map{ case (focal, count) => FocalElement(focal, count) }
        Ok(Extraction.decompose(result))
      } else Forbidden
    }
  }

  def iso(userId: Long) = Action.async{ implicit req =>
    auth(userId).map { enabled =>
      if(enabled) {
        val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
        val counts = Image.groupByCondCount(where, cond.iso)(AutoSession, implicitly[TypeBinder[Int]])
        val result = counts.map{ case (iso, count) => IsoElement(iso, count) }
        Ok(Extraction.decompose(result))
      } else Forbidden
    }
  }

  def fNumber(userId: Long) = Action.async{ implicit req =>
    auth(userId).map{ enabled =>
      if(enabled) {
        val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
        val counts = Image.groupByCondCount(where, cond.fNumber)(AutoSession, implicitly[TypeBinder[Double]])
        val result = counts.map{ case (fNumber, count) => FNumberElement(fNumber, count) }
        Ok(Extraction.decompose(result))
      } else Forbidden
    }
  }

  def exposure(userId: Long) = Action.async{ implicit req =>
    auth(userId).map{ enabled =>
      if(enabled) {
        val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
        val counts = Image.groupByCondCount(where, cond.exposure)(AutoSession, implicitly[TypeBinder[Int]])
        val result = counts.map{ case (exposure, count) => ExposureElement(exposure, count) }
        Ok(Extraction.decompose(result))
      } else Forbidden
    }
  }

  def lens(userId: Long) = Action.async{ implicit req =>
    auth(userId).map{ enabled =>
      if(enabled) {
        val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
        val counts = Image.groupByCount(where, i.lensId)(AutoSession, implicitly[TypeBinder[Long]])
        val lens: Map[Long, String] = Lens.findAll().map{ l => l.id -> l.name }(breakOut)
        val result = counts.map{ case (id, count) => LensElement(id, lens(id), count) }
        Ok(Extraction.decompose(result))
      } else Forbidden
    }
  }

  def camera(userId: Long) = Action.async{ implicit req =>
    auth(userId).map{ enabled =>
      if(enabled) {
        val where = sqls.eq(i.userId, userId).and(GraphFilter.fromReq(req).where)
        val counts = Image.groupByCount(where, i.cameraId)(AutoSession, implicitly[TypeBinder[Long]])
        val cameras: Map[Long, String] = Camera.findAll().map{ c => c.id -> s"${c.name}(${c.maker})" }(breakOut)
        val result = counts.map{ case (id, count) => CameraElement(id, cameras(id), count) }
        Ok(Extraction.decompose(result))
      } else Forbidden
    }
  }

  def auth(userId: Long)(implicit req: RequestHeader, ec: ExecutionContext): Future[Boolean] = {
    User.findById(userId).fold(Future.successful(false)) { user =>
      if(user.public) Future.successful(true)
      else {
        req.cookies.get("PLAY2AUTH_SESS_ID").fold(Future.successful(false)){ cookie =>
          val token = cookie.value.takeRight(SessionContainer.TokenSize)
          idContainer.get(token).map { optUser =>
            optUser.contains(userId)
          }
        }
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
