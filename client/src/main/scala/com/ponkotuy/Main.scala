package com.ponkotuy

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Path, Paths}

import com.ponkotuy.queries.{Exif, LoginEmail}
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import org.json4s.native.Serialization.write
import skinny.http.{HTTP, Request}

import scala.compat.java8.StreamConverters._

object Main {
  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  val config = new Config

  def main(args: Array[String]): Unit = {
    val extractor = new com.ponkotuy.Extractor
    login().fold(println("Auth failed")){ session =>
      args.foreach{ raw =>
        val exifs = files(raw).filter(_.toString.toLowerCase.endsWith(".jpg")).flatMap{ file =>
          val map = extractor.read(file).tagMaps
          map.filterNot(_._1.startsWith("Unknown tag")).filter(_._1.startsWith("Lens")).foreach(println)
          Exif.fromMap(file.getFileName.toString, map)
        }
        exifs.foreach{ exif =>
          println(exif.fileName)
          val req = Request(s"${config.server}/api/exif")
              .header("Cookie", session.toString)
              .body(write(exif).getBytes(Charset.forName("UTF-8")), "application/json")
          val res = HTTP.post(req)
          if(res.status / 100 != 2) println(new String(res.body))
        }
      }
    }
  }

  private def login(): Option[Cookie] = {
    val auth = LoginEmail(config.email, config.password)
    val json = write(auth).getBytes(StandardCharsets.UTF_8)
    val req = Request(s"${config.server}/api/session").body(json, "application/json")
    val res = HTTP.post(req)
    res.header("Set-Cookie").map(Cookie.fromStr)
  }

  private def files(raw: String): List[Path] = {
    val path = Paths.get(raw)
    if(Files.isDirectory(path)) Files.list(path).toScala[List]
    else path :: Nil
  }
}
