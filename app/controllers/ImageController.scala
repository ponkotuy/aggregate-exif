package controllers

import java.io.File
import java.util.Locale
import java.util.zip.ZipFile
import javax.inject.{Inject, Singleton}

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.github.tototoshi.play2.json4s.native.Json4s
import com.ponkotuy.Extractor
import com.ponkotuy.queries.{Exif, ExifParseError}
import jp.t2v.lab.play2.auth.AuthElement
import models.{ExifSerializer, Image}
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Extraction}
import play.api.Logger
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{Controller, Result}
import requests.{Content, FileContent, FileExtension, ZipContent}
import scalikejdbc._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

@Singleton
class ImageController @Inject()(_ec: ExecutionContext, json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import ImageController._
  import Responses._
  import json4s._

  implicit def ec: ExecutionContext = _ec
  implicit def formats = DefaultFormats ++ JodaTimeSerializers.all
  val extractor = new Extractor

  val ParPage = 100
  def list(page: Int) = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    import models.Aliases.i
    val images = if(page < 10)
      Image.findAllByWithLimitOffset(
        sqls.eq(i.userId, loggedIn.id),
        limit = ParPage,
        offset = page * ParPage,
        orderings = i.id.desc :: Nil)
    else Nil
    Ok(Extraction.decompose(images))
  }

  def count() = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    import models.Aliases.i
    val count = Image.countBy(sqls.eq(i.userId, loggedIn.id))
    Ok(count.toString)
  }

  def upload = StackAction(parse.multipartFormData, AuthorityKey -> NormalUser) { implicit req =>
    req.body.file("file").fold(notFound("file element")){ file =>
      parseFile(file).right.map{ content =>
        val map = content.metadata(extractor).tagMaps
        Exif.fromMap(content.fileName, map).fold[Result](parseError, { exif =>
          DB localTx { implicit session =>
            if(new ExifSerializer(exif).save(loggedIn.id).exists(0 < _)) {
              Logger.info(s"Insert ${exif.fileName}")
              Success
            } else BadRequest("Duplicate files(not insert)")
          }
        })
      }.merge
    }
  }
}

object ImageController {
  import FileExtension._
  import Responses._
  import utils.EitherUtil._

  def parseFile(file: FilePart[TemporaryFile]): Either[Result, Content] = {
    for {
      extRaw <- extractExt(file.filename).toRight(notFound("extension from file name"))
      _ <- FileExtension.find(extRaw).filter(_ == JPEG).toRight(BadRequest("Unsupported file"))
    } yield {
      FileContent(file.filename, file.ref.file)
    }
  }

  def decodeZipToJpegs(file: File): Iterator[ZipContent] = {
    val zf = new ZipFile(file)
    zf.entries().asScala.filterNot(_.isDirectory)
        .map { entry => ZipContent(entry.getName, zf.getInputStream(entry)) }
  }

  private def extractExt(fileName: String): Option[String] =
    fileName.toLowerCase(Locale.ENGLISH).split('.').lastOption

  private def parseError(error: ExifParseError): Result = {
    val mes = s"ParseError: ${error} not found."
    Logger.warn(mes)
    BadRequest(mes)
  }
}
