
name := "AggregateEXIFLibrary"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "com.github.nscala-time" %% "nscala-time" % "2.16.0",
  "com.drewnoakes" % "metadata-extractor" % "2.10.1",
  "org.json4s" %% "json4s-native" % "3.5.2",
  "org.json4s" %% "json4s-ext" % "3.5.2"
)
