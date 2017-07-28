package requests

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
