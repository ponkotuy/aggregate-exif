package models

import authes.Role
import com.github.nscala_time.time.Imports._
import io.circe._
import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import utils.DateTimeCodec

case class User(id: Long, name: String, email: String, role: Role, password: String, createdAt: DateTime) {
  lazy val minimal = UserMinimal(id = id, name = name,createdAt = createdAt)
}

object User extends SkinnyCRUDMapperWithId[Long, User] with DateTimeCodec {
  override val tableName = "account"
  override val defaultAlias: Alias[User] = createAlias("u")
  override def extract(rs: WrappedResultSet, n: ResultName[User]): User = new User(
    id = rs.get(n.id),
    name = rs.get(n.name),
    email = rs.get(n.email),
    role = Role.find(rs.get(n.role)).get,
    password = rs.get(n.password),
    createdAt = rs.get(n.createdAt)
  )

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def create(u: User)(implicit session: DBSession): Long =
    createWithAttributes(
      'name -> u.name,
      'email -> u.email,
      'role -> u.role.value,
      'password -> u.password,
      'createdAt -> u.createdAt
    )

  implicit val encode: Encoder[User] = io.circe.generic.semiauto.deriveEncoder[User]
  implicit val decode: Decoder[User] = io.circe.generic.semiauto.deriveDecoder[User]
}

case class UserMinimal(id: Long, name: String, createdAt: DateTime)

object UserMinimal extends DateTimeCodec {
  implicit val encode: Encoder[UserMinimal] = io.circe.generic.semiauto.deriveEncoder[UserMinimal]
  implicit val decode: Decoder[UserMinimal] = io.circe.generic.semiauto.deriveDecoder[UserMinimal]
}
