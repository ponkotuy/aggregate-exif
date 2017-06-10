package com.ponkotuy

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class Exif(
    iso: Int,
    focal: Int,
    focal35: Int,
    fNumber: Double,
    exposure: Int,
    dateTime: LocalDateTime,
    camera: Camera,
    lens: Lens)

object Exif {
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

  def fromMap(map: Map[String, String]): Option[Exif] = for {
    iso <- map.get("ISO Speed Ratings")
    focal <- map.get("Focal Length").map(extractNumber)
    focal35 <- map.get("Focal Length 35").map(extractNumber)
    fNumber <- map.get("F-Number").map(extractDouble)
    exposure <- map.get("Exposure Time").map(extractExposure)
    dateTime <- map.get("Date/Time").map { str => LocalDateTime.parse(str, formatter) }
    camera <- Camera.fromMap(map)
    lens <- Lens.fromMap(map)
  } yield Exif(iso.toInt, focal, focal35, fNumber, exposure, dateTime, camera, lens)

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
