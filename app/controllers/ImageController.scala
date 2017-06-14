package controllers

import javax.inject.{Inject, Singleton}

import com.ponkotuy.{Exif, Extractor}
import models.ExifSerializer
import play.api.mvc.{Action, Controller}
import scalikejdbc.DB

@Singleton
class ImageController @Inject() extends Controller {
  import Responses._

  val extractor = new Extractor
  def upload = Action(parse.multipartFormData) { req =>
    req.body.file("file").fold(notFound("file element")) { file =>
      val map = extractor.read(file.ref.file).tagMaps
      Exif.fromMap(map).fold(notFound("EXIF information")){ exif =>
        DB localTx { implicit session =>
          if(0L < new ExifSerializer(exif).save(0L)) Success else InternalServerError("DBError")
        }
      }
    }
  }
}
