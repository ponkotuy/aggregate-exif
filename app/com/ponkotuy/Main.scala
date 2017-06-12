package com.ponkotuy

import java.nio.file.{Files, Paths}

import scala.compat.java8.StreamConverters._
import scalax.chart.exporting.PNGExporter
import scalax.chart.module.Charting

object Main extends Charting {
  def main(args: Array[String]): Unit = {
    val extractor = new com.ponkotuy.Extractor
    val exifs = Files.list(Paths.get("/home/yosuke/103_PANA/")).toScala[List]
        .filter(_.toString.endsWith(".JPG")).flatMap { file =>
      val map = extractor.read(file).tagMaps
      Exif.fromMap(map)
    }
    aggISO(exifs).saveAsPNG("/tmp/iso.png")
    val zoom: Seq[(Int, Int)] = exifs.groupBy(_.focal).mapValues(_.size).toSeq.sortBy(_._1)
    XYBarChart(zoom).saveAsPNG("/tmp/zoom.png")
    val fNumber: Seq[(Double, Int)] = exifs.groupBy(_.fNumber).mapValues(_.size).toSeq.sortBy(_._1)
    XYBarChart(fNumber).saveAsPNG("/tmp/fNumber.png")
    aggExposure(exifs).saveAsPNG("/tmp/exposure.png")
  }

  val ISOGroup: Stream[Int] = 100 #:: ISOGroup.map(_ * 2)
  def aggISO(exifs: Seq[Exif]): PNGExporter = {
    val iso: Seq[(Int, Int)] = exifs.map { x => x.copy(iso = ISOGroup.find(x.iso <= _).get) }
        .groupBy(_.iso).mapValues(_.size).toSeq.sortBy(_._1)
    BarChart(iso)
  }

  val ExposureGroup = Seq(-30, -15, -8, -4, -2, 1, 2, 4, 8, 15, 30, 60, 120, 250, 500, 1000, 2000, 4000, 8000, 16000)
  def aggExposure(exifs: Seq[Exif]): PNGExporter = {
    val exposure: Seq[(Int, Int)] = exifs.map { x => x.copy(exposure = ExposureGroup.find(x.exposure <= _).get) }
        .groupBy(_.exposure).mapValues(_.size).toSeq.sortBy(_._1)
    BarChart(exposure)
  }
}
