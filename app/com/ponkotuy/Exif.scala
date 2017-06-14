package com.ponkotuy

import com.github.nscala_time.time.Imports._
import org.joda.time.format.DateTimeFormatter

case class Exif(
    fileName: String,
    cond: Exif.PhotoCond,
    dateTime: DateTime,
    camera: Exif.Camera,
    lens: Exif.Lens)

object Exif {
  val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy:MM:dd HH:mm:ss")

  def fromMap(map: Map[String, String]): Option[Exif] = for {
    cond <- PhotoCond.fromMap(map)
    dateTime <- map.get("Date/Time").map { str => formatter.parseDateTime(str) }
    camera <- Camera.fromMap(map)
    lens <- Lens.fromMap(map)
  } yield Exif("", cond, dateTime, camera, lens)

  case class Camera(maker: String, name: String)

  object Camera {
    def fromMap(map: Map[String, String]): Option[Camera] = for {
      maker <- map.get("Make")
      model <- map.get("Model")
    } yield Camera(maker, model)
  }

  case class Lens(name: String)

  object Lens {
    def fromMap(map: Map[String, String]): Option[Lens] = for {
      name <- map.get("Lens Type")
    } yield Lens(name)
  }

  case class PhotoCond(iso: Int, focal: Int, focal35: Int, fNumber: Double, exposure: Int)

  object PhotoCond {
    def fromMap(map: Map[String, String]): Option[PhotoCond] = for {
      iso <- map.get("ISO Speed Ratings").map(_.toInt)
      focal <- map.get("Focal Length").map(extractNumber)
      focal35 <- map.get("Focal Length 35").map(extractNumber)
      fNumber <- map.get("F-Number").map(extractDouble)
      exposure <- map.get("Exposure Time").map(extractExposure)
    } yield PhotoCond(iso, focal, focal35, fNumber, exposure)

    private def extractNumber(str: String): Int =
      str.filter { c => '0' <= c && c <= '9' }.toInt

    private def extractDouble(str: String): Double =
      str.filter { c => c == '.' || '0' <= c && c <= '9' }.toDouble

    // if 1/60 => 60, 1" -> -1, 2" => -2
    private def extractExposure(str: String): Int = {
      if(str.contains('"')) -extractNumber(str)
      else extractNumber(str.dropWhile(_ == '/').tail)
    }
  }
}
