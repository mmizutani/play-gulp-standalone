import PlayGulp._

name := "play-gulp-standalone"

version := "1.0"

scalaVersion := "2.11.7"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(playGulpSettings)
  // optionally enable the capability to compile twirl templates under ui/src directory
  //.settings(withTemplates)

// Required by specs2 to get scalaz-stream
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  specs2 % Test
)

routesGenerator := InjectedRoutesGenerator