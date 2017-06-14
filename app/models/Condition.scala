package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Condition(
    id: Long,
    imageId: Long,
    iso: Int,
    focal: Int,
    focal35: Int,
    fNumber: Double,
    exposure: Int
)

object Condition extends SkinnyCRUDMapperWithId[Long, Condition] {
  override val defaultAlias: Alias[Condition] = createAlias("co")
  override def extract(rs: WrappedResultSet, n: ResultName[Condition]): Condition = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def create(cond: Condition)(implicit session: DBSession): Long = createWithAttributes(
    'imageId -> cond.imageId,
    'iso -> cond.iso,
    'focal -> cond.focal,
    'focal35 -> cond.focal35,
    'fNumber -> cond.fNumber,
    'exposure -> cond.exposure
  )
}
