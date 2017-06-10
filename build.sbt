
scalaVersion := "2.12.2"

name := "AggregateEXIF"

libraryDependencies ++= Seq(
  "com.drewnoakes" % "metadata-extractor" % "2.10.1",
  "com.github.wookietreiber" %% "scala-chart" % "0.5.1",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0"
)
