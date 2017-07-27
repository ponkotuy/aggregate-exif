
name := "AggregateEXIFClient"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
  "org.skinny-framework" %% "skinny-http-client" % "2.3.7",
  "org.json4s" %% "json4s-native" % "3.5.2",
  "org.json4s" %% "json4s-ext" % "3.5.2",
  "com.typesafe" % "config" % "1.3.1",
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "ch.qos.logback" % "logback-classic" % "1.1.7" % "runtime",
  "org.slf4j" % "slf4j-api" % "1.7.21"

)
