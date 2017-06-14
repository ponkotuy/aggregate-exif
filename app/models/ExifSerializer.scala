package models

import com.github.nscala_time.time.Imports._
import com.ponkotuy.Exif
import scalikejdbc.DBSession

class ExifSerializer(exif: Exif) {
  def save(userId: Long)(implicit session: DBSession): Long = {
    import ExifSerializer._
    val lensId = saveLens(exif.lens)
    val cameraId = saveCamera(exif.camera)
    val imageId = saveImage(exif)(userId, cameraId, lensId)
    saveCond(exif.cond)(imageId)
    imageId
  }
}

object ExifSerializer {
  private def saveLens(lens: Exif.Lens)(implicit session: DBSession): Long = {
    Lens.create(lens.name)
  }

  private def saveCamera(camera: Exif.Camera)(implicit session: DBSession): Long = {
    Camera.create(new Camera(0L, camera.maker, camera.name))
  }

  private def saveImage(exif: Exif)(userId: Long, cameraId: Long, lensId: Long)(implicit session: DBSession): Long = {
    val image = new Image(0L, userId, exif.fileName, cameraId, lensId, exif.dateTime, DateTime.now())
    Image.create(image)
  }

  private def saveCond(cond: Exif.PhotoCond)(imageId: Long)(implicit session: DBSession): Long = {
    val condition = new Condition(0L, imageId, cond.iso, cond.focal, cond.focal35, cond.fNumber, cond.exposure)
    Condition.create(condition)
  }
}
