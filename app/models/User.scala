package models

import authes.Role
import com.github.nscala_time.time.Imports._
import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class User(
    id: Long,
    name: String,
    email: String,
    role: Role,
    password: String,
    public: Boolean,
    createdAt: DateTime) {
  lazy val minimal = UserMinimal(id = id, name = name, public = public, createdAt = createdAt)
}

object User extends SkinnyCRUDMapperWithId[Long, User] {
  override val tableName = "account"
  override val defaultAlias: Alias[User] = createAlias("u")
  override def extract(rs: WrappedResultSet, n: ResultName[User]): User = new User(
    id = rs.get(n.id),
    name = rs.get(n.name),
    email = rs.get(n.email),
    role = Role.find(rs.get(n.role)).get,
    password = rs.get(n.password),
    public = rs.get(n.public),
    createdAt = rs.get(n.createdAt)
  )

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def attributes(u: User) = Seq(
    'name -> u.name,
    'email -> u.email,
    'role -> u.role.value,
    'password -> u.password,
    'public -> u.public,
    'createdAt -> u.createdAt
  )

  def create(u: User)(implicit session: DBSession): Long = createWithAttributes(attributes(u):_*)

  def update(u: User)(implicit session: DBSession): Int = updateById(u.id).withAttributes(attributes(u):_*)
}

case class UserMinimal(id: Long, name: String, public: Boolean, createdAt: DateTime)
