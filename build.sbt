val http4sVersion = "0.18.12"
val specs2Version = "4.2.0"
val log4CatsVersion = "0.0.6"
val slf4jVersion = "1.7.25"
val kindProjectorVersion = "0.9.6"
val circeVersion = "0.9.2"

lazy val root = (project in file("."))
  .settings(
    organization := "ru.pavkin",
    name := "telegram-bot-fs2",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.6",
    libraryDependencies ++= Seq(
      compilerPlugin("org.spire-math" %% "kind-projector" % kindProjectorVersion),
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.chrisdavenport" %% "log4cats-core" % log4CatsVersion,
      "io.chrisdavenport" %% "log4cats-slf4j" % log4CatsVersion,
      "org.slf4j" % "slf4j-simple" % slf4jVersion,
      "org.specs2" %% "specs2-core" % specs2Version % "test"
    )
  )

