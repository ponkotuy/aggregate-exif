package com.ponkotuy

import java.io.File

import com.drew.imaging.jpeg.JpegMetadataReader
import com.drew.metadata.Tag

import scala.collection.JavaConverters._
import scala.collection.breakOut

class Extractor {
  def read(fName: String): Metadata = {
    new Metadata(JpegMetadataReader.readMetadata(new File(fName)))
  }
}

class Metadata(orig: com.drew.metadata.Metadata) {
  def directories: Iterable[Directory] = orig.getDirectories.asScala.map(new Directory(_))
  def tags: Iterable[Tag] = directories.flatMap(_.tags)
  def tagMaps: Map[String, String] = tags.map { tag => tag.getTagName -> tag.getDescription }(breakOut)
}

class Directory(orig: com.drew.metadata.Directory) {
  def tags: Iterable[Tag] = orig.getTags.asScala
  def name = orig.getName
}
