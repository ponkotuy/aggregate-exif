package com.ponkotuy

object Main {
  def main(args: Array[String]): Unit = {
    val extractor = new com.ponkotuy.Extractor
    val map = extractor.read("/home/yosuke/103_PANA/P1030001.JPG").tagMaps
    println(Exif.fromMap(map))
  }
}
