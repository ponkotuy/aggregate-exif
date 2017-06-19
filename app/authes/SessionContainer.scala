package authes

import java.security.SecureRandom

import jp.t2v.lab.play2.auth.{AuthenticityToken, IdContainer}
import models.Session
import scalikejdbc._
import com.github.nscala_time.time.Imports._

import scala.annotation.tailrec
import scala.util.Random

class SessionContainer extends IdContainer[Long] {
  import SessionContainer._

  val random = new Random(new SecureRandom())

  override def startNewSession(userId: Long, timeoutInSeconds: Int): AuthenticityToken = {
    val now = DateTime.now()
    val token = generate
    Session(0L, userId, token, now, now + timeoutInSeconds.seconds).save()(AutoSession)
    token
  }

  override def remove(token: AuthenticityToken): Unit =
    Session.deleteBy(sqls.eq(Session.column.token, token))

  override def get(token: AuthenticityToken): Option[Long] =
    Session.findByToken(token).map(_.userId)

  override def prolongTimeout(token: AuthenticityToken, timeoutInSeconds: Int): Unit =
    Session.findByToken(token).map { s =>
      s.copy(expire = DateTime.now() + timeoutInSeconds.seconds).save()
    }

  @tailrec
  private final def generate: AuthenticityToken = {
    val table = "abcdefghijklmnopqrstuvwxyz1234567890_.~*'()"
    val token = Iterator.continually(random.nextInt(table.length)).map(table).take(TokenSize).mkString
    if (get(token).isDefined) generate else token
  }

}

object SessionContainer {
  val TokenSize = 64
  implicit val db: DBSession = AutoSession
}
