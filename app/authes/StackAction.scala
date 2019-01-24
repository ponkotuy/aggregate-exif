package authes

import models.{Session, User}
import play.api.mvc.Results._
import play.api.mvc.{Action, AnyContent, BodyParser, Cookie, Request, Result}
import scalikejdbc.{AutoSession, DB, DBSession}
import utils.{TokenGenerator, TokenTable}
import com.github.nscala_time.time.Imports._

case class LoggedInWithDB[T](req: Request[T], session: Session, user: User, db: DBSession)
case class LoggedIn[T](req: Request[T], session: Session, user: User) {
  def withDB(db: DBSession) = LoggedInWithDB(req, session, user, db)
}

object StackAction {
  val SessionName = "PLAY2AUTH_SESS_ID"
  val Expire = 7.days

  private[this] val generator = new TokenGenerator(TokenTable.LegacyTable)

  def apply[T](role: Role, parser: BodyParser[T])(f: LoggedInWithDB[T] => Result): Action[T] = Action(parser) { req =>
    common(role)(req)(f)
  }

  def apply(role: Role)(f: LoggedInWithDB[AnyContent] => Result): Action[AnyContent] =
    Action { req => common[AnyContent](role)(req)(f) }

  def common[T](role: Role)(req: Request[T])(f: LoggedInWithDB[T] => Result): Result = {
    DB localTx { implicit db =>
      auth(role)(req).fold(Forbidden("")) { req =>
        f(req.withDB(db))
      }
    }
  }

  def auth[T](role: Role)(req: Request[T])(implicit db: DBSession = AutoSession): Option[LoggedIn[T]] = {
    for {
      token <- req.cookies.get(SessionName)
      session <- Session.findByToken(token.value)
      user <- User.findById(session.userId)
      if role.checkAuthority(user)
    } yield LoggedIn(req, session, user)
  }

  def genSession(user: User)(implicit db: DBSession): Cookie = {
    val token = generator.generate(64)
    val now = DateTime.now()
    val expired = now + Expire
    Session(0L, user.id, token, now, expired).save()
    Cookie(SessionName, token, maxAge = Some(Expire.toStandardSeconds.getSeconds))
  }
}
