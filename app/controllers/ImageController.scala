package controllers

import java.io.{File, InputStream}
import java.util.Locale
import java.util.zip.ZipFile
import javax.inject.{Inject, Singleton}

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.ponkotuy.queries.{Exif, ExifParseError}
import com.ponkotuy.{Extractor, Metadata}
import jp.t2v.lab.play2.auth.AuthElement
import models.ExifSerializer
import play.api.Logger
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{Controller, Result}
import scalikejdbc.DB

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

@Singleton
class ImageController @Inject()(_ec: ExecutionContext) extends Controller with AuthElement with AuthConfigImpl {
  import ImageController._
  import Responses._

  implicit def ec: ExecutionContext = _ec

  val extractor = new Extractor
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

sealed abstract class FileExtension {
  def isMine(ext: String): Boolean
}

object FileExtension {
  case object JPEG extends FileExtension {
    override def isMine(ext: String): Boolean = Seq("jpg", "jpeg", "jpe").contains(ext)
  }

  case object ZIP extends FileExtension {
    override def isMine(ext: String): Boolean = Seq("zip", "jar").contains(ext)
  }

  val values = Seq(JPEG, ZIP)

  def find(ext: String) = values.find(_.isMine(ext))
}

trait Content {
  def fileName: String
  def metadata(extractor: Extractor): Metadata
}

case class ZipContent(fileName: String, is: InputStream) extends Content {
  override def metadata(extractor: Extractor): Metadata = extractor.read(is)
}

case class FileContent(fileName: String, file: File) extends Content {
  override def metadata(extractor: Extractor): Metadata = extractor.read(file)
}
