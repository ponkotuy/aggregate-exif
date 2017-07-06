package controllers

import com.amazonaws.services.simpleemail.model.{Body, Destination, Content => SESContent}
import com.github.tototoshi.play2.json4s.native.Json4s
import com.google.inject.Inject
import models.{PasswordReset, User}
import org.json4s.DefaultFormats
import play.api.Configuration
import play.api.mvc.{Action, Controller, Result}
import queries.{BCryptEncoder, RequestReset, ResetPassword}
import scalikejdbc.{AutoSession, DB, sqls}
import utils.{Config, Mail, MyAmazonSES}

import scala.concurrent.duration._

class PasswordController @Inject() (json4s: Json4s, _config: Configuration) extends Controller {
  import PasswordController._
  import Responses._
  import json4s._

  implicit val formats = DefaultFormats
  val conf = new Config(_config)
  lazy val ses: Option[MyAmazonSES] = conf.amazon.flatMap(_.region).map(new MyAmazonSES(_))

  def request() = Action(json) { implicit req =>
    import models.Aliases.u
    import utils.EitherUtil.eitherToRightProjection
    val result = for {
      rr <- req.body.extractOpt[RequestReset].toRight(JsonParseError)
      account <- User.findBy(sqls.eq(u.email, rr.email)).toRight(notFound(s"email(${rr.email})"))
    } yield {
      PasswordReset.deleteByAccountId(account.id)(AutoSession)
      val reset = PasswordReset.fromAccountId(account.id)
      reset.save()(AutoSession)
      val mes = message(reset.secret)
      val result = for {
        from <- conf.mail
        mail = Mail(new Destination().withToAddresses(account.email), Title, mes, from)
        client <- ses
      } yield client.send(mail)
      result.fold(InternalServerError("Sending mail error"))(_ => Success)
    }
    result.merge
  }

  def reset() = Action(json) { implicit request =>
    import models.Aliases.pr
    import utils.EitherUtil.eitherToRightProjection
    val result: Either[Result, Result] = for {
      req <- request.body.extractOpt[ResetPassword].toRight(JsonParseError)
      db <- PasswordReset.findBy(sqls.eq(pr.secret, req.secret)).toRight(notFound(s"PasswordReset secret: ${req.secret}")).right
      _ <- Either.cond(System.currentTimeMillis() - 1.days.toMillis < db.created, Left(()), RequestTimeout("Timeout"))
      account <- User.findById(db.accountId).toRight(notFound(s"Account: ${db.accountId}"))
    } yield {
      DB localTx { implicit session =>
        User.update(account.copy(password = BCryptEncoder(req.password)))
        PasswordReset.deleteByAccountId(account.id)
      }
      Success
    }
    result.merge
  }
}

object PasswordController {
  val Title = content("CameraNamazuのパスワードリセット")

  def message(secret: String) = new Body().withText(
    content(
      s"""
         |※このメールはAmazonSESによって送信されています
         |
         |CameraNamazuのパスワードリセットメールです。
         |心当たりのない方は無視するか返信で教えていただけると幸いです。
         |
         |以下のURLにてパスワードを再設定することができます。
         |
         |https://exif.ponkotuy.com/auth/password_reset.html?secret=${secret}
         |
         |有効期間は1日間のみです。それ以降は再送してください
      """.stripMargin
    )
  )

  def content(data: String) = new SESContent().withData(data)
}
