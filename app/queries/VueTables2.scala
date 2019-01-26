package queries

import play.api.mvc.RequestHeader
import scalikejdbc.interpolation.SQLSyntax

case class VueTables2(
    query: Option[String],
    limit: Int,
    page: Int,
    orderBy: SQLSyntax,
    byColumn: Option[String]
)

trait VueTablesParser {
  def ParPage: Int = 100
  def PageOrigin: Int = 0

  def column(str: String): Option[SQLSyntax]
  def defaultOrderColumn: SQLSyntax
  def defaultAscending: Boolean

  def fromReq(req: RequestHeader): VueTables2 = {
    val isAscending = req.getQueryString("ascending").exists(_.toInt == 1)
    val orderBy: SQLSyntax = req.getQueryString("orderBy").flatMap(column).getOrElse(defaultOrderColumn)
    VueTables2(
      req.getQueryString("query").filter(_.nonEmpty),
      req.getQueryString("limit").map(_.toInt).getOrElse(ParPage),
      req.getQueryString("page").map(_.toInt - 1).getOrElse(PageOrigin),
      if(isAscending) orderBy.asc else orderBy.desc,
      req.getQueryString("byColumn")
    )
  }
}

object ImageTable extends VueTablesParser {
  import models.Aliases.i

  override def column(str: String): Option[SQLSyntax] = str match {
    case "name" => Some(i.fileName)
    case "shootingTime" => Some(i.dateTime)
    case "uploadingTime" => Some(i.createdAt)
    case _ => None
  }

  override def defaultOrderColumn: SQLSyntax = i.dateTime
  override def defaultAscending: Boolean = false
}
