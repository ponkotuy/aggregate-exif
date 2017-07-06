package utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model._

class MyAmazonSES(region: Regions) {
  lazy val client = AmazonSimpleEmailServiceClientBuilder.standard()
      .withRegion(region)
      .build()

  def send(mail: Mail): Unit = {
    val req = new SendEmailRequest()
      .withSource(mail.from)
      .withDestination(mail.dest)
      .withMessage(mail.message)
    client.sendEmail(req)
  }
}

case class Mail(dest: Destination, subject: Content, body: Body, from: String) {
  def message: Message = new Message().withSubject(subject).withBody(body)
}
