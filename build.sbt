
scalaVersion := "2.11.11"

name := "AggregateEXIF"

lazy val library = (project in file("library"))
lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .dependsOn(library)
lazy val client = (project in file("client"))
  .dependsOn(library)

resolvers += "Bintary JCenter" at "http://jcenter.bintray.com"

libraryDependencies ++= Seq(
  "org.skinny-framework" %% "skinny-orm" % "2.3.7",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.5.3",
  "org.flywaydb" %% "flyway-play" % "3.1.0",
  "org.postgresql" % "postgresql" % "9.4.1212",
  "org.springframework.security" % "spring-security-web" % "4.2.3.RELEASE",
  "com.github.nscala-time" %% "nscala-time" % "2.16.0",
  "jp.t2v" %% "play2-auth" % "0.14.2",
  "com.github.tototoshi" %% "play-json4s-native" % "0.7.0",
  "org.json4s" %% "json4s-ext" % "3.5.2",
  "ch.qos.logback" % "logback-classic" % "1.1.7" % "runtime",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "com.amazonaws" % "aws-java-sdk" % "1.11.158"
)
