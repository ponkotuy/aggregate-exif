package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Camera(id: Long, maker: String, name: String)

object Camera extends SkinnyCRUDMapperWithId[Long, Camera] {
  override val defaultAlias: Alias[Camera] = createAlias("c")
  override def extract(rs: WrappedResultSet, n: ResultName[Camera]): Camera = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def create(c: Camera)(implicit session: DBSession): Long =
    createWithAttributes('maker -> c.maker, 'name -> c.name)
}
