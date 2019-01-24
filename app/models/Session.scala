package models

import com.github.nscala_time.time.Imports._
import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Session(
    id: Long,
    userId: Long,
    token: String,
    createdAt: DateTime,
    expire: DateTime
) {

  def save()(implicit db: DBSession): Long = {
    if (id <= 0) Session.create(this) else Session.update(this)
  }
}

object Session extends SkinnyCRUDMapperWithId[Long, Session] {
  import Aliases.s

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  override val defaultAlias: Alias[Session] = createAlias("se")

  override def extract(rs: WrappedResultSet, n: ResultName[Session]): Session = autoConstruct(rs, n)

  def findByToken(token: String)(implicit db: DBSession): Option[Session] =
    findBy(sqls.eq(s.token, token).and.gt(s.expire, DateTime.now))

  def create(se: Session)(implicit db: DBSession): Long =
    createWithAttributes(params(se): _*)

  def update(se: Session)(implicit db: DBSession): Long =
    updateById(se.id).withAttributes(params(se): _*).toLong

  def params(se: Session) = Seq(
    'userId -> se.userId,
    'token -> se.token,
    'createdAt -> se.createdAt,
    'expire -> se.expire
  )
}
