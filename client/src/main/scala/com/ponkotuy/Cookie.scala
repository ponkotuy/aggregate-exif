package com.ponkotuy

import scala.collection.breakOut

case class Cookie(raw: Map[String, String]) {
  override def toString: String = raw.map { case (k, v) => s"$k=$v" }.mkString(";")
}

object Cookie {
  def fromStr(str: String): Cookie = {
    val raw: Map[String, String] = str.split(';').flatMap { part =>
      part.trim.split('=') match {
        case Array(k) => Some(k -> "")
        case Array(k, v) => Some(k -> v)
        case _ => None
      }
    }(breakOut)
    Cookie(raw)
  }

  def empty = Cookie(Map.empty)
}
