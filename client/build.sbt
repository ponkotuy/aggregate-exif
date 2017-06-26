
name := "AggregateEXIFClient"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
  "org.skinny-framework" %% "skinny-http-client" % "2.3.7",
  "org.json4s" %% "json4s-native" % "3.5.2",
  "org.json4s" %% "json4s-ext" % "3.5.2"
)
