import PlayGulp._

name := "play-gulp-standalone"

version := "1.1"

scalaVersion := "2.11.8"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(playGulpSettings)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0-RC1" % Test
)

routesGenerator := InjectedRoutesGenerator