package utils

import com.github.nscala_time.time.Imports._
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import org.joda.time.format.ISODateTimeFormat

trait DateTimeCodec {
  val formatter = ISODateTimeFormat.dateTime()
  implicit val dateTimeEncoder: Encoder[DateTime] = new Encoder[DateTime] {
    override def apply(a: DateTime): Json = Json.fromString(a.toString(formatter))
  }
  implicit val dateTimedecoder: Decoder[DateTime] = new Decoder[DateTime] {
    override def apply(c: HCursor): Result[DateTime] = c.as[String].right.map(formatter.parseDateTime)
  }
}
