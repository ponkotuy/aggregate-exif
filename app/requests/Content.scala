package requests

import java.io.{File, InputStream}

import com.ponkotuy.{Extractor, Metadata}

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
