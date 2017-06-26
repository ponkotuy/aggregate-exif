package controllers

import javax.inject.{Inject, Singleton}

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.native.Json4s
import com.ponkotuy.queries.Exif
import jp.t2v.lab.play2.auth.AuthElement
import models.ExifSerializer
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import play.api.Logger
import play.api.mvc.Controller
import scalikejdbc.DB

@Singleton
class ExifController @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import Responses._
  import json4s._

  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  def upload() = StackAction(json, AuthorityKey -> NormalUser) { implicit req =>
    req.body.extractOpt[Exif].fold(JsonParseError){ exif =>
      println(exif)
      DB localTx { implicit session =>
        if(new ExifSerializer(exif).save(loggedIn.id).exists(0 < _)) {
          Logger.info(s"Insert ${exif.fileName}")
          Success
        } else BadRequest("Duplicated?")
      }
    }
  }
}
