package queries

import play.api.mvc.RequestHeader
import scalikejdbc._

case class VueTables2(
    query: Map[String, String],
    limit: Int,
    page: Int,
    orderBy: SQLSyntax,
    byColumn: Option[String],
    parser: VueTablesParser
) {
  def where: SQLSyntax = {
    val xs: Seq[Option[SQLSyntax]] = query.map { case (k, v) =>
      for {
        col <- parser.column(k)
        value <- parser.toValue(col, v)
      } yield value
    }.toSeq
    sqls.toAndConditionOpt(xs:_*).getOrElse(sqls"true")
  }
}

trait VueTablesParser {
  import VueTablesParser.QueryKeyFormat
  def ParPage: Int = 100
  def PageOrigin: Int = 0

  def column(str: String): Option[SQLSyntax]
  def toValue(col: SQLSyntax, value: String): Option[SQLSyntax]
  def defaultOrderColumn: SQLSyntax
  def defaultAscending: Boolean

  def fromReq(req: RequestHeader): VueTables2 = {
    val query = req.queryString.filterKeys(_.startsWith("query")).flatMap { case (k, v) =>
      k match {
        case QueryKeyFormat(col) => v.map(col -> _)
        case _ => None
      }
    }
    val isAscending = req.getQueryString("ascending").exists(_.toInt == 1)
    val orderBy: SQLSyntax = req.getQueryString("orderBy").flatMap(column).getOrElse(defaultOrderColumn)
    VueTables2(
      query,
      req.getQueryString("limit").map(_.toInt).getOrElse(ParPage),
      req.getQueryString("page").map(_.toInt - 1).getOrElse(PageOrigin),
      if(isAscending) orderBy.asc else orderBy.desc,
      req.getQueryString("byColumn"),
      this
    )
  }
}

object VueTablesParser {
  val QueryKeyFormat = """query\[(.*)\]""".r
}

object ImageTable extends VueTablesParser {
  import models.Aliases.{i, cond}

  override def column(str: String): Option[SQLSyntax] = str match {
    case "fileName" => Some(i.fileName)
    case "cond.fNumber" => Some(cond.fNumber)
    case "cond.focal" => Some(cond.focal)
    case "cond.iso" => Some(cond.iso)
    case "dateTime" => Some(i.dateTime)
    case "createdAt" => Some(i.createdAt)
    case _ => None
  }

  override def toValue(col: SQLSyntax, value: String): Option[SQLSyntax] = {
    if(col == i.fileName) Some(sqls.eq(col, value))
    else if(col == cond.fNumber) Some(sqls.eq(col, BigDecimal(value)))
    else if(col == cond.focal) Some(sqls.eq(col, BigDecimal(value)))
    else if(col == cond.iso) Some(sqls.eq(col, value.toInt))
    else if(col == i.dateTime) Some(sqls.eq(col, value))
    else if(col == i.createdAt) Some(sqls.eq(col, value))
    else None
  }

  override def defaultOrderColumn: SQLSyntax = i.dateTime
  override def defaultAscending: Boolean = false
}
