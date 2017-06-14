package models

import com.github.nscala_time.time.Imports._
import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class User(id: Long, name: String, createdAt: DateTime)

object User extends SkinnyCRUDMapperWithId[Long, User] {
  override val defaultAlias: Alias[User] = createAlias("u")
  override def extract(rs: WrappedResultSet, n: ResultName[User]): User = new User(
    id = rs.get(n.id),
    name = rs.get(n.name),
    createdAt = rs.get(n.createdAt)
  )

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong
}
