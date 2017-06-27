package com.ponkotuy

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Paths}

import com.ponkotuy.queries.{Exif, LoginEmail}
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import org.json4s.native.Serialization.write
import skinny.http.{HTTP, Request}

import scala.compat.java8.StreamConverters._

object Main {
  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  val Host = "http://localhost:9000"

  def main(args: Array[String]): Unit = {
    val extractor = new com.ponkotuy.Extractor
    login("i@ponkotuy.com", "yamamot1").fold(println("Auth failed")){ session =>
      args.foreach{ raw =>
        val exifs = Files.list(Paths.get(raw)).toScala[List]
            .filter(_.toString.endsWith(".JPG")).flatMap{ file =>
          val map = extractor.read(file).tagMaps
          Exif.fromMap(file.getFileName.toString, map)
        }
        exifs.foreach{ exif =>
          println(exif.fileName)
          val req = Request(s"${Host}/api/exif")
              .header("Cookie", session.toString)
              .body(write(exif).getBytes(Charset.forName("UTF-8")), "application/json")
          val res = HTTP.post(req)
          if(res.status / 100 != 2) println(new String(res.body))
        }
      }
    }
  }

  def login(email: String, pass: String): Option[Cookie] = {
    val auth = LoginEmail(email, pass)
    val json = write(auth).getBytes(StandardCharsets.UTF_8)
    val req = Request(s"${Host}/api/session").body(json, "application/json")
    val res = HTTP.post(req)
    println(res.rawCookies)
    res.header("Set-Cookie").map(Cookie.fromStr)
  }
}
