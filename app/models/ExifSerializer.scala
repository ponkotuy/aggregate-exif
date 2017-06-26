package models

import com.github.nscala_time.time.Imports._
import com.ponkotuy.queries.Exif
import scalikejdbc._

class ExifSerializer(exif: Exif) {
  def save(userId: Long)(implicit session: DBSession): Option[Long] = {
    import ExifSerializer._
    val lensId = searchLensId(exif.lens)
    val cameraId = searchCameraId(exif.camera)
    saveImage(exif)(userId, cameraId, lensId).map { imageId =>
      saveCond(exif.cond)(imageId)
      imageId
    }
  }
}

object ExifSerializer {
  private def searchLensId(lens: Exif.Lens)(implicit session: DBSession): Long = {
    import Aliases.l
    Lens.findBy(sqls.eq(l.name, lens.name)).fold(Lens.create(lens.name))(_.id)
  }

  private def searchCameraId(camera: Exif.Camera)(implicit session: DBSession): Long = {
    import Aliases.c
    Camera.findBy(sqls.eq(c.name, camera.name).and.eq(c.maker, camera.maker))
        .fold(Camera.create(new Camera(0L, camera.maker, camera.name)))(_.id)
  }

  private def saveImage(exif: Exif)(userId: Long, cameraId: Long, lensId: Long)(implicit session: DBSession): Option[Long] = {
    import Aliases.i
    val exists = Image.findBy(sqls.eq(i.userId, userId).and.eq(i.fileName, exif.fileName)).isDefined
    println(exists)
    if(exists) None
    else {
      val image = new Image(0L, userId, exif.fileName, cameraId, lensId, exif.dateTime, DateTime.now())
      Some(Image.create(image))
    }
  }

  private def saveCond(cond: Exif.PhotoCond)(imageId: Long)(implicit session: DBSession): Long = {
    val condition = new Condition(0L, imageId, cond.iso, cond.focal, cond.focal35, cond.fNumber, cond.exposure)
    Condition.create(condition)
  }
}
