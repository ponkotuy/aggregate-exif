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

//    aggISO(exifs).saveAsPNG("/tmp/iso.png")
//    val zoom: Seq[(Int, Int)] = exifs.groupBy(_.cond.focal).mapValues(_.size).toSeq.sortBy(_._1)
//    XYBarChart(zoom).saveAsPNG("/tmp/zoom.png")
//    val fNumber: Seq[(Double, Int)] = exifs.groupBy(_.cond.fNumber).mapValues(_.size).toSeq.sortBy(_._1)
//    XYBarChart(fNumber).saveAsPNG("/tmp/fNumber.png")
//    aggExposure(exifs).saveAsPNG("/tmp/exposure.png")
//  }
//
//  val ISOGroup: Stream[Int] = 100 #:: ISOGroup.map(_ * 2)
//  def aggISO(exifs: Seq[Exif]): PNGExporter = {
//    val iso: Seq[(Int, Int)] = exifs.map { x => x.cond.copy(iso = ISOGroup.find(x.cond.iso <= _).get) }
//        .groupBy(_.iso).mapValues(_.size).toSeq.sortBy(_._1)
//    BarChart(iso)
//  }
//
//  val ExposureGroup = Seq(-30, -15, -8, -4, -2, 1, 2, 4, 8, 15, 30, 60, 120, 250, 500, 1000, 2000, 4000, 8000, 16000)
//  def aggExposure(exifs: Seq[Exif]): PNGExporter = {
//    val exposure: Seq[(Int, Int)] = exifs.map { x => x.cond.copy(exposure = ExposureGroup.find(x.cond.exposure <= _).get) }
//        .groupBy(_.exposure).mapValues(_.size).toSeq.sortBy(_._1)
//    BarChart(exposure)
//  }
}
