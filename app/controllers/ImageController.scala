package controllers

import javax.inject.{Inject, Singleton}

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.ponkotuy.{Exif, Extractor}
import jp.t2v.lab.play2.auth.AuthElement
import models.ExifSerializer
import play.api.mvc.Controller
import scalikejdbc.DB

@Singleton
class ImageController @Inject() extends Controller with AuthElement with AuthConfigImpl {
  import Responses._

  val extractor = new Extractor
  def upload = StackAction(parse.multipartFormData, AuthorityKey -> NormalUser) { implicit req =>
    req.body.file("file").fold(notFound("file element")) { file =>
      val map = extractor.read(file.ref.file).tagMaps
      Exif.fromMap(map).fold(notFound("EXIF information")){ exif =>
        DB localTx { implicit session =>
          if(0L < new ExifSerializer(exif).save(loggedIn.id)) Success else InternalServerError("DBError")
        }
      }
    }
  }
}
