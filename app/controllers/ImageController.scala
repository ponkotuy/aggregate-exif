package controllers

import java.io.File
import java.util.Locale
import java.util.zip.ZipFile

import authes.Role.NormalUser
import authes.StackAction
import com.github.tototoshi.play2.json4s.native.Json4s
import com.ponkotuy.Extractor
import com.ponkotuy.queries.{Exif, ExifParseError}
import javax.inject.{Inject, Singleton}
import models.{ExifSerializer, Image}
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Extraction}
import play.api.Logger
import play.api.libs.Files
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{InjectedController, MultipartFormData, Result}
import queries._
import scalikejdbc._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

@Singleton
class ImageController @Inject()(_ec: ExecutionContext, json4s: Json4s) extends InjectedController {
  import ImageController._
  import Responses._
  import json4s.implicits._

  implicit def ec: ExecutionContext = _ec
  implicit def formats = DefaultFormats ++ JodaTimeSerializers.all
  val extractor = new Extractor

  val ParPage = 100
  def list(page: Int) = StackAction(NormalUser) { req =>
    import models.Aliases.i
    if(page < 0) BadRequest("Page is negative")
    else {
      val images = if(page < 10)
        Image.findAllByWithLimitOffset(
          sqls.eq(i.userId, req.user.id),
          limit = ParPage,
          offset = page * ParPage,
          orderings = i.id.desc :: Nil)(req.db)
      else Nil
      Ok(Extraction.decompose(images))
    }
  }

  def count() = StackAction(NormalUser) { req =>
    import models.Aliases.i
    val count = Image.countBy(sqls.eq(i.userId, req.user.id))(req.db)
    val page = PageCount(((count + ParPage - 1) / ParPage).toInt, count)
    Ok(Extraction.decompose(page))
  }

  def upload() = StackAction(NormalUser) { req =>
    val res = for {
      multipart <- req.req.body.asMultipartFormData.toRight(BadRequest("Accept only multipart/form-data"))
      file <- multipart.file("file").toRight(notFound("file element"))
      content <- parseFile(file)
      map = content.metadata(extractor).tagMaps
      exif <- Exif.fromMap(content.fileName, map).left.map(parseError)
    } yield {
      if(new ExifSerializer(exif).save(req.user.id)(req.db).exists(0 < _)) {
        Logger.info(s"Insert ${exif.fileName}")
        Success
      } else BadRequest("Duplicate files(not insert)")
    }
    res.merge
  }

  def delete() = StackAction(NormalUser, json4s.json) { req =>
    req.req.body.extractOpt[Seq[Long]].fold(JsonParseError) { xs =>
      Image.deleteBy(sqls.in(Image.column.id, xs).and.eq(Image.column.userId, req.user.id))
      Success
    }
  }
}

object ImageController {
  import FileExtension._
  import Responses._

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

case class PageCount(page: Int, count: Long)
