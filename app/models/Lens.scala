package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Lens(id: Long, name: String)

object Lens extends SkinnyCRUDMapperWithId[Long, Lens] {
  override val defaultAlias: Alias[Lens] = createAlias("l")
  override def extract(rs: WrappedResultSet, n: ResultName[Lens]): Lens = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def create(name: String)(implicit session: DBSession): Long =
    createWithAttributes('name -> name)
}
