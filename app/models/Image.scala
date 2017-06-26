package models

import com.github.nscala_time.time.Imports._
import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Image(
    id: Long,
    userId: Long,
    fileName: String,
    cameraId: Long,
    lensId: Long,
    dateTime: DateTime,
    createdAt: DateTime,
    camera: Option[Camera] = None,
    lens: Option[Lens] = None,
    cond: Option[Condition] = None
)

object Image extends SkinnyCRUDMapperWithId[Long, Image] {
  override val defaultAlias: Alias[Image] = createAlias("i")
  override def extract(rs: WrappedResultSet, n: ResultName[Image]): Image = new Image(
    id = rs.long(n.id),
    userId = rs.long(n.userId),
    fileName = rs.get(n.fileName),
    cameraId = rs.get(n.cameraId),
    lensId = rs.get(n.lensId),
    dateTime = rs.jodaDateTime(n.dateTime),
    createdAt = rs.jodaDateTime(n.createdAt)
  )

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  belongsTo[Camera](
    right = Camera,
    merge = (image, camera) => image.copy(camera = camera)
  ).byDefault

  belongsTo[Lens](
    right = Lens,
    merge = (image, lens) => image.copy(lens = lens)
  ).byDefault

  hasOne[Condition](
    right = Condition,
    merge = (image, cond) => image.copy(cond = cond)
  ).byDefault

  def create(i: Image)(implicit session: DBSession): Long = createWithAttributes(
    'userId -> i.userId,
    'fileName -> i.fileName,
    'cameraId -> i.cameraId,
    'lensId -> i.lensId,
    'dateTime -> i.dateTime,
    'createdAt -> i.createdAt
  )

  def groupByCount[T](where: SQLSyntax, col: SQLSyntax)(typeBinder: TypeBinder[T])(implicit session: DBSession): Seq[(T, Int)] = withSQL {
    import Aliases.{i, cond}
    select(col, sqls.count(sqls"*")).from(Image as i)
        .innerJoin(Condition as cond).on(i.id, cond.imageId)
        .where(where)
        .groupBy(col)
        .orderBy(col)
  }.map { rs =>
    val value = rs.get[T](1)(typeBinder)
    val count = rs.int(2)
    (value, count)
  }.collection.apply()
}
