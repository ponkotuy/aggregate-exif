package com.ponkotuy

case class Exif(iso: Int, focal: Int, camera: Camera)

object Exif {
  def fromMap(map: Map[String, String]): Option[Exif] = for {
    iso <- map.get("ISO Speed Ratings")
    _focal <- map.get("Focal Length")
    focal = _focal.takeWhile { c => '0' <= c && c <= '9' }.toInt
    camera <- Camera.fromMap(map)
  } yield Exif(iso.toInt, focal.toInt, camera)
}

case class Camera(maker: String, name: String)

object Camera {
  def fromMap(map: Map[String, String]): Option[Camera] = for {
    maker <- map.get("Make")
    model <- map.get("Model")
  } yield Camera(maker, model)

}
