package models

import java.util.UUID

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class PasswordReset(id: Long, accountId: Long, secret: String, created: Long) {
  def save()(implicit session: DBSession) = PasswordReset.create(this)
}

object PasswordReset extends SkinnyCRUDMapperWithId[Long, PasswordReset] {
  override val defaultAlias: Alias[PasswordReset] = createAlias("pr")

  override def extract(rs: WrappedResultSet, n: ResultName[PasswordReset]): PasswordReset = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id

  override def rawValueToId(value: Any): Long = value.toString.toLong

  def create(pr: PasswordReset)(implicit session: DBSession): Long =
    createWithAttributes('accountId -> pr.accountId, 'secret -> pr.secret, 'created -> pr.created)

  def fromAccountId(accountId: Long): PasswordReset =
    new PasswordReset(0L, accountId, UUID.randomUUID().toString, System.currentTimeMillis())

  def deleteByAccountId(accountId: Long)(implicit session: DBSession) =
    PasswordReset.deleteBy(sqls.eq(column.accountId, accountId))
}
