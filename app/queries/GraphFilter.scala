package queries

import com.github.nscala_time.time.Imports._
import org.joda.time.format.ISODateTimeFormat
import play.api.mvc.RequestHeader
import scalikejdbc._

case class GraphFilter(
    focal: Option[Int],
    iso: Option[Int],
    fNumber: Option[Double],
    exposure: Option[Int],
    camera: Option[Long],
    lens: Option[Long],
    start: Option[DateTime],
    end: Option[DateTime]
) {
  def where:Option[SQLSyntax] = {
    import models.Aliases.{cond, i}
    sqls.toAndConditionOpt(
      focal.map { v => sqls.eq(cond.focal, v) },
      iso.map { v => sqls.eq(cond.iso, v) },
      fNumber.map { v => sqls.eq(cond.fNumber, v) },
      exposure.map { v => sqls.eq(cond.exposure, v) },
      camera.map { v => sqls.eq(i.cameraId, v) },
      lens.map { v => sqls.eq(i.lensId, v) },
      start.map { v => sqls.ge(i.dateTime, v) },
      end.map { v => sqls.le(i.dateTime, v) }
    )
  }
}

object GraphFilter {
  val format = ISODateTimeFormat.dateTimeNoMillis()
  def fromReq(req: RequestHeader): GraphFilter = {
    GraphFilter(
      req.getQueryString("focal").map(_.toInt),
      req.getQueryString("iso").map(_.toInt),
      req.getQueryString("fNumber").map(_.toDouble),
      req.getQueryString("exposure").map(_.toInt),
      req.getQueryString("camera").map(_.toLong),
      req.getQueryString("lens").map(_.toLong),
      req.getQueryString("start").map(format.parseDateTime),
      req.getQueryString("end").map(format.parseDateTime)
    )
  }
}
