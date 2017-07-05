package queries

import authes.Role
import com.github.nscala_time.time.Imports._
import models.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

case class CreateUser(name: String, email: String, password: String) {
  def user(now: DateTime = DateTime.now()): User = {
    val encoded = BCryptEncoder(password)
    User(0L, name, email, Role.NormalUser, encoded, public = false, now)
  }
}

object BCryptEncoder {
  val bcrypt = new BCryptPasswordEncoder(12)
  def apply(pass: String) = bcrypt.encode(pass)
}
