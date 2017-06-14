package com.ponkotuy

import java.io.File
import java.nio.file.Path

import com.drew.imaging.jpeg.JpegMetadataReader
import com.drew.metadata.Tag

import scala.collection.JavaConverters._
import scala.collection.breakOut

class Extractor {
  def read(fName: String): Metadata =
    read(new File(fName))
  def read(path: Path): Metadata =
    read(path.toFile)
  def read(file: File): Metadata =
    new Metadata(JpegMetadataReader.readMetadata(file))
}

class Metadata(orig: com.drew.metadata.Metadata) {
  def directories: Iterable[Directory] = orig.getDirectories.asScala.map(new Directory(_))
  def tags: Iterable[Tag] = directories.flatMap(_.tags)
  def tagMaps: Map[String, String] = tags.map { tag => tag.getTagName -> tag.getDescription }(breakOut)
}

class Directory(orig: com.drew.metadata.Directory) {
  def tags: Iterable[Tag] = orig.getTags.asScala
  def name: String = orig.getName
}
