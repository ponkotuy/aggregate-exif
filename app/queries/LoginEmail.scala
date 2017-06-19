package queries

import io.circe._
import models.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import scalikejdbc._

case class LoginEmail(email: String, password: String) {
  import LoginEmail._

  def authenticate()(implicit session: DBSession): Option[User] = {
    User.findBy(sqls.eq(User.column.email, email)).filter { user =>
      bcrypt.matches(password, user.password)
    }
  }
}

object LoginEmail {
  val bcrypt = new BCryptPasswordEncoder()
  implicit val encode: Encoder[LoginEmail] = io.circe.generic.semiauto.deriveEncoder[LoginEmail]
  implicit val decode: Decoder[LoginEmail] = io.circe.generic.semiauto.deriveDecoder[LoginEmail]
}
