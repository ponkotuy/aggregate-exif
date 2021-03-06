
scalaVersion := "2.12.8"

name := "AggregateEXIF"

lazy val root = (project in file("."))
    .enablePlugins(PlayScala)

resolvers += "Bintary JCenter" at "http://jcenter.bintray.com"

libraryDependencies ++= Seq(
  guice,
  "org.skinny-framework" %% "skinny-orm" % "3.0.1",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.6.0-scalikejdbc-3.3",
  "org.scalikejdbc" %% "scalikejdbc-joda-time" % "3.3.2",
  "org.flywaydb" %% "flyway-play" % "5.2.0",
  "org.postgresql" % "postgresql" % "9.4.1212",
  "org.springframework.security" % "spring-security-web" % "4.2.3.RELEASE",
  "com.github.nscala-time" %% "nscala-time" % "2.16.0",
  "com.github.tototoshi" %% "play-json4s-native" % "0.8.0",
  "org.json4s" %% "json4s-ext" % "3.5.2",
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "ch.qos.logback" % "logback-classic" % "1.1.7" % "runtime",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "com.amazonaws" % "aws-java-sdk" % "1.11.158",
  "com.drewnoakes" % "metadata-extractor" % "2.11.0",
  "io.sentry" % "sentry-logback" % "1.7.16"
)

javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)

// Docker
dockerBaseImage := "openjdk:11-slim"
dockerRepository := Some("ponkotuy")
dockerUpdateLatest := true

// Speedup build
sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false
