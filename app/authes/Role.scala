package authes

import scalikejdbc.TypeBinder

sealed abstract class Role(val value: Int)

object Role {
  case object Disabled extends Role(-1)
  case object Administrator extends Role(0)
  case object NormalUser extends Role(1)

  val values = Seq(Disabled, Administrator, NormalUser)
  def find(value: Int): Option[Role] = values.find(_.value == value)

  implicit def typeBinder: TypeBinder[Int] = TypeBinder.int
  implicit val impl: TypeBinder[Role] = TypeBinder(_ getInt _)(_ getInt _).map { i => find(i).getOrElse(NormalUser) }
}
