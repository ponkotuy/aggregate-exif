package controllers

import authes.Role.NormalUser
import authes.StackAction
import com.github.tototoshi.play2.json4s.native.Json4s
import com.ponkotuy.queries.Exif
import javax.inject.{Inject, Singleton}
import models.{ExifSerializer, Image}
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Extraction}
import play.api.Logger
import play.api.mvc.InjectedController
import scalikejdbc._

@Singleton
class ExifController @Inject()(json4s: Json4s) extends InjectedController {
  import Responses._
  import json4s.implicits._

  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  def upload() = StackAction(NormalUser, json4s.json) { req =>
    req.req.body.extractOpt[Exif].fold(JsonParseError){ exif =>
      DB localTx { implicit session =>
        if(new ExifSerializer(exif).save(req.user.id).exists(0 < _)) {
          Logger.info(s"Insert ${exif.fileName}")
          Success
        } else BadRequest("Duplicated?")
      }
    }
  }

  def list(userId: Long) = Action {
    import models.Aliases.i
    Ok(Extraction.decompose(Image.findAllBy(sqls.eq(i.userId, userId))))
  }
}
