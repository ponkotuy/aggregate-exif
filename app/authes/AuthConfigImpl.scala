package authes

import jp.t2v.lab.play2.auth._
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait AuthConfigImpl extends AuthConfig {
  import controllers.Responses._

  override type Id = Long
  override type User = models.User
  override type Authority = Role

  override lazy val idContainer = AsyncIdContainer(new SessionContainer())

  override val idTag: ClassTag[Id] = implicitly[ClassTag[Id]]

  override def sessionTimeoutInSeconds: Int = 7.days.toSeconds.toInt

  override def resolveUser(id: Id)(implicit context: ExecutionContext): Future[Option[User]] =
    Future.successful(models.User.findById(id))

  override def loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] =
    Future.successful(Success)

  override def logoutSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] =
    Future.successful(Success)

  override def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] =
    Future.successful(Forbidden("Authentication failed"))

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Role])(implicit context: ExecutionContext): Future[Result] =
    Future.successful(Forbidden("Authorization failed"))

  override def authorize(user: User, authority: Authority)(implicit context: ExecutionContext): Future[Boolean] = Future.successful {
    (user.role, authority) match {
      case (Role.Disabled, _) => false
      case (Role.Administrator, _) => true
      case (Role.NormalUser, Role.NormalUser) => true
      case _ => false
    }
  }

  override lazy val tokenAccessor = new CookieTokenAccessor(
    cookieSecureOption = false,
    cookieMaxAge = Some(sessionTimeoutInSeconds)
  )
}
