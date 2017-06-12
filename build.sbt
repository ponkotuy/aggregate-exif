
scalaVersion := "2.11.11"

name := "AggregateEXIF"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  "com.drewnoakes" % "metadata-extractor" % "2.10.1",
  "com.github.wookietreiber" %% "scala-chart" % "0.5.1",
  "com.itextpdf" % "itextpdf" % "5.5.11",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
  "org.flywaydb" %% "flyway-play" % "3.1.0",
  "org.postgresql" % "postgresql" % "9.4.1212"
)
