package controllers

import java.io.{File, InputStream}
import java.util.Locale
import java.util.zip.ZipFile
import javax.inject.{Inject, Singleton}

import authes.AuthConfigImpl
import authes.Role.NormalUser
import com.ponkotuy.{Exif, Extractor, Metadata}
import jp.t2v.lab.play2.auth.AuthElement
import models.ExifSerializer
import play.api.Logger
import play.api.libs.Files.TemporaryFile
import play.api.mvc.Controller
import play.api.mvc.MultipartFormData.FilePart
import scalikejdbc.DB

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImageController @Inject()(_ec: ExecutionContext) extends Controller with AuthElement with AuthConfigImpl {
  import ImageController._
  import Responses._

  implicit def ec: ExecutionContext = _ec

  val extractor = new Extractor
  def upload = StackAction(parse.multipartFormData, AuthorityKey -> NormalUser) { implicit req =>
    req.body.file("file").fold(notFound("file element")){ file =>
      Future {
        parseFile(file).foreach{ content =>
          val map = content.metadata(extractor).tagMaps
          Exif.fromMap(content.fileName, map).foreach { exif =>
            DB localTx { implicit session =>
              if(new ExifSerializer(exif).save(loggedIn.id).exists(0 < _)) Logger.info(s"Insert ${exif.fileName}")
            }
          }
        }
      }
      SeeOther("/")
    }
  }
}

object ImageController {
  import FileExtension._
  def parseFile(file: FilePart[TemporaryFile]): TraversableOnce[Content] = {
    val res = for {
      extRaw <- extractExt(file.filename)
      ext <- FileExtension.find(extRaw)
    } yield {
      ext match {
        case JPEG => FileContent(file.filename, file.ref.file) :: Nil
        case ZIP => decodeZipToJpegs(file.ref.file)
      }
    }
    res.getOrElse(Nil)
  }

  def decodeZipToJpegs(file: File): Iterator[ZipContent] = {
    val zf = new ZipFile(file)
    zf.entries().asScala.filterNot(_.isDirectory)
        .map { entry => ZipContent(entry.getName, zf.getInputStream(entry)) }
  }

  private def extractExt(fileName: String): Option[String] =
    fileName.toLowerCase(Locale.ENGLISH).split('.').lastOption
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
