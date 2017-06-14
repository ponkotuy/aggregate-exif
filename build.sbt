
scalaVersion := "2.11.11"

name := "AggregateEXIF"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  "com.drewnoakes" % "metadata-extractor" % "2.10.1",
  "com.github.wookietreiber" %% "scala-chart" % "0.5.1",
  "com.itextpdf" % "itextpdf" % "5.5.11",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
  "org.skinny-framework" %% "skinny-orm" % "2.3.7",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.5.3",
  "org.flywaydb" %% "flyway-play" % "3.1.0",
  "org.postgresql" % "postgresql" % "9.4.1212",
  "com.github.nscala-time" %% "nscala-time" % "2.16.0"
)
