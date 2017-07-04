package queries

import com.github.nscala_time.time.Imports._
import org.joda.time.format.ISODateTimeFormat
import play.api.mvc.RequestHeader
import scalikejdbc._

import scala.util.Try

case class GraphFilter(
    focal: Option[Range[Int]],
    iso: Option[Range[Int]],
    fNumber: Option[Range[Double]],
    exposure: Option[Range[Int]],
    camera: Option[Long],
    lens: Option[Long],
    start: Option[DateTime],
    end: Option[DateTime]
) {
  def where:Option[SQLSyntax] = {
    import models.Aliases.{cond, i}
    sqls.toAndConditionOpt(
      focal.map(_.where(cond.focal35)),
      iso.map(_.where(cond.iso)),
      fNumber.map(_.where(cond.fNumber)),
      exposure.map(_.where(cond.exposure)),
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
      req.getQueryString("focal").flatMap(Range.fromString(_.toInt)),
      req.getQueryString("iso").flatMap(Range.fromString(_.toInt)),
      req.getQueryString("fNumber").flatMap(Range.fromString(_.toDouble)),
      req.getQueryString("exposure").flatMap(Range.fromString(_.toInt)),
      req.getQueryString("camera").map(_.toLong),
      req.getQueryString("lens").map(_.toLong),
      req.getQueryString("start").map(format.parseDateTime),
      req.getQueryString("end").map(format.parseDateTime)
    )
  }
}

case class Range[A](start: A, end: A) {
  def where(column: SQLSyntax)(implicit factory: ParameterBinderFactory[A]) =
    if(start == end) sqls.eq(column, start)
    else sqls.ge(column, start).and.lt(column, end)
}

object Range {
  def fromString[A](f: String => A)(str: String): Option[Range[A]] = Try {
    val Array(start, end) = str.split('_')
    Range(f(start), f(end))
  }.orElse {
    Try {
      val value = f(str)
      Range(value, value)
    }
  }.toOption
}
