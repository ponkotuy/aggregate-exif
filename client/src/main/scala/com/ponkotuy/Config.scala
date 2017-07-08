package com.ponkotuy

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}

import com.typesafe.config.ConfigFactory

class Config {
  import Config._
  val directory = Paths.get(System.getProperty("user.home")).resolve(".namazu")
  Files.createDirectories(directory)
  val file =  directory.resolve("application.conf")
  if(!Files.isRegularFile(file)) printToFile(file.toFile)(Initial)
  val config = ConfigFactory.parseFile(file.toFile)

  val server = config.getString("server")
  val email = config.getString("email")
  val password = config.getString("password")
}

object Config {
  val Initial =
    """
      |server = "https://namazu.ponkotuy.com"
      |email = ""
      |password = ""
      |""".stripMargin

  def printToFile(file: File)(str: String): Unit = {
    val p = new PrintWriter(file)
    try p.print(str) finally p.close()
  }
}
