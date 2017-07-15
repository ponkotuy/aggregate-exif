package controllers

import javax.inject.{Inject, Singleton}

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.native.Json4s
import com.ponkotuy.queries.Exif
import jp.t2v.lab.play2.auth.AuthElement
import models.{ExifSerializer, Image}
import org.json4s.{DefaultFormats, Extraction}
import org.json4s.ext.JodaTimeSerializers
import play.api.Logger
import play.api.mvc.{Action, Controller}
import scalikejdbc._

@Singleton
class ExifController @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import json4s._

  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  def upload() = StackAction(json, AuthorityKey -> NormalUser) { implicit req =>
    req.body.extractOpt[Exif].fold(JsonParseError){ exif =>
      DB localTx { implicit session =>
        if(new ExifSerializer(exif).save(loggedIn.id).exists(0 < _)) {
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
