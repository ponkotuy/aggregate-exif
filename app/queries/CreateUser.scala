package queries

import authes.Role
import models.User
import com.github.nscala_time.time.Imports._
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

case class CreateUser(name: String, email: String, password: String) {
  def user(now: DateTime = DateTime.now()): User = {
    val encoded = BCryptEncoder(password)
    User(0L, name, email, Role.NormalUser, encoded, now)
  }
}

object BCryptEncoder {
  val bcrypt = new BCryptPasswordEncoder()
  def apply(pass: String) = bcrypt.encode(pass)
}
