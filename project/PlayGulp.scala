import sbt._
import sbt.Keys._
import play.sbt.PlayRunHook
import play.sbt.PlayImport.PlayKeys._
import play.twirl.sbt.Import.TwirlKeys
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._
import com.typesafe.sbt.web.Import._

object PlayGulp {

  lazy val gulpDirectory = SettingKey[File]("gulp-directory", "gulp directory")
  lazy val gulpFile = SettingKey[String]("gulp-file", "gulpfile")
  lazy val distExcludes = SettingKey[Seq[String]]("dist-excludes")
  lazy val gulpExcludes = SettingKey[Seq[String]]("gulp-excludes")
  lazy val gulp = InputKey[Unit]("gulp", "Task to run gulp")
  lazy val gulpBuild = TaskKey[Int]("gulp-dist", "Task to run dist gulp")
  lazy val gulpClean = TaskKey[Unit]("gulp-clean", "Task to run gulp clean")
  lazy val gulpTest = TaskKey[Unit]("gulp-test", "Task to run gulp test")
  lazy val postStageClean = taskKey[Unit]("Clean unnecessary build files after stage task")

  val playGulpSettings: Seq[Setting[_]] = Seq(
    // Specifies the location of the root directory of the Gulp project relative to the Play app root
    gulpDirectory <<= (baseDirectory in Compile) { _ / "ui" },

    gulpFile := "gulpfile.js",

    gulp := {
      val base = (gulpDirectory in Compile).value
      val gulpfileName = (gulpFile in Compile).value
      runGulp(base, gulpfileName, Def.spaceDelimited("<arg>").parsed.toList).exitValue()
    },

    gulpBuild := {
      val base = (gulpDirectory in Compile).value
      val gulpfileName = (gulpFile in Compile).value
      val result = runGulp(base, gulpfileName, List("build")).exitValue()
      if (result == 0) {
        result
      } else throw new Exception("gulp failed")
    },

    gulpClean := {
      val base = (gulpDirectory in Compile).value
      val gulpfileName = (gulpFile in Compile).value
      val result = runGulp(base, gulpfileName, List("clean")).exitValue()
      if (result != 0) throw new Exception("gulp failed")
    },

    gulpTest := {
      val base = (gulpDirectory in Compile).value
      val gulpfileName = (gulpFile in Compile).value
      val result = runGulp(base, gulpfileName, List("test")).exitValue()
      if (result != 0) throw new Exception("gulp failed")
    },

    // Executes `gulp build` before `sbt dist`
    dist <<= dist dependsOn gulpBuild,

    // Executes `gulp build` before `sbt stage`
    stage <<= stage dependsOn gulpBuild,

    // Executes `gulp clean` before `sbt clean`
    clean <<= clean dependsOn gulpClean,

    // Executes `gulp test` before `sbt test` (optional)
    //(test in Test) <<= (test in Test) dependsOn gulpTest,

    // Ensures that static assets in the ui directory are packaged into
    // target/scala-2.11/play-gulp-standalone_2.11-x.x.x-web-asset.jar/public when the play app is compiled in the stage and dist tasks
    unmanagedResourceDirectories in Assets <+= gulpDirectory,

    // FIXME: The following does not correctly apply excludeFilter to unmanagedResourceDirectoy.
    // So we use alternative solutions to reduce slug size for Heroku deploy
    // https://devcenter.heroku.com/articles/reducing-the-slug-size-of-play-2-x-applications
//    distExcludes <<= gulpDirectory(gd => Seq(
//      gd + "/src/",
//      gd + "/build/",
//      gd + "/bower_components/",
//      gd + "/jspm_packages/",
//      gd + "/node_modules/"
//    )),
//    //https://github.com/sbt/sbt/blob/291059a72b56b009a3af32d811cea81b9e632c1f/main/src/main/scala/sbt/Defaults.scala#L212-L223
//    //http://mariussoutier.com/blog/2014/12/07/understanding-sbt-sbt-web-settings/
//    excludeFilter in Assets <<=
//      (excludeFilter in Assets,
//        distExcludes in Assets) {
//        (currentFilter: FileFilter, de) =>
//          currentFilter || new FileFilter {
//            def accept(pathname: File): Boolean = {
//              (true /: de.map(s => pathname.getAbsolutePath.startsWith(s)))(_ && _)
//            }
//          }
//      },
  
    // Starts the gulp watch task before sbt run
    playRunHooks <+= (gulpDirectory, gulpFile).map {
      (base, fileName) => GulpWatch(base, fileName)
    },

    // Allows all the specified commands below to be run within sbt in addition to gulp
    commands <++= gulpDirectory {
      base =>
        Seq(
          "npm",
          "bower",
          "yo",
          "jspm",
          "ied",
          "npmd",
          "git"
        ).map(cmd(_, base))
    }
  )

  val withTemplates: Seq[Setting[_]] = Seq(
    // Added ui/src/views/*.scala.html to the target of Scala view template compilation
    sourceDirectories in TwirlKeys.compileTemplates in Compile <+= (gulpDirectory in Compile)(_ / "src"),
    includeFilter in sources in TwirlKeys.compileTemplates := "*.scala.html",
    gulpExcludes <<= gulpDirectory(gd => Seq(
      gd + "/src/app/",
      gd + "/src/dist/",
      gd + "/src/bower_components/"
    )),
    excludeFilter in unmanagedSources <<=
      (excludeFilter in unmanagedSources, gulpExcludes) {
        (currentFilter: FileFilter, ge) =>
          currentFilter || new FileFilter {
            def accept(pathname: File): Boolean = {
              (true /: ge.map(s => pathname.getAbsolutePath.startsWith(s)))(_ && _)
            }
          }
      },
    // Makes play autoreloader to compile Scala view templates and reload the browser
    // upon changes in the view files ui/src/views/*.scala.html
    // Adds ui/src/views directory's scala view template files to continuous hot reloading
    watchSources <++= gulpDirectory map { path => ((path / "src/views") ** "*.scala.html").get}
  )

  // To run this task, set the Heroku environment variable on command line
  //   heroku config:set SBT_POST_TASKS=postStageClean
  // or by adding the following to app.json.
  //   "env": { "SBT_POST_TASKS": "postStageClean" }
  // https://github.com/heroku/heroku-buildpack-scala/commit/fdaa1159b8f75909e55566b12a222afef486cf05
  postStageClean := {
    val exclDirs = List("src", "build", "bower_components")
    exclDirs.foreach( dir =>
      sbt.IO.delete(gulpDirectory.value / dir)
    )
  }

  private def runGulp(base: sbt.File, fileName: String, args: List[String] = List.empty): Process = {
    if (System.getProperty("os.name").startsWith("Windows")) {
      val process: ProcessBuilder = Process("cmd" :: "/c" :: "gulp" :: "--gulpfile=" + fileName :: args, base)
      println(s"Will run: ${process.toString} in ${base.getPath}")
      process.run()
    } else {
      val process: ProcessBuilder = Process("gulp" :: "--gulpfile=" + fileName :: args, base)
      println(s"Will run: ${process.toString} in ${base.getPath}")
      process.run()
    }
  }

  import scala.language.postfixOps

  private def cmd(name: String, base: File): Command = {
    if (!base.exists()) {
      base.mkdirs()
    }
    Command.args(name, "<" + name + "-command>") {
      (state, args) =>
        if (System.getProperty("os.name").startsWith("Windows")) {
          Process("cmd" :: "/c" :: name :: args.toList, base) !<
        } else {
          Process(name :: args.toList, base) !<
        }
        state
    }
  }

  object GulpWatch {

    def apply(base: File, fileName: String): PlayRunHook = {

      object GulpSubProcessHook extends PlayRunHook {

        var process: Option[Process] = None

        override def beforeStarted(): Unit = {
          process = Some(runGulp(base, fileName, "watch" :: Nil))
        }

        override def afterStopped(): Unit = {
          process.foreach(_.destroy())
          process = None
        }
      }

      GulpSubProcessHook
    }

  }

}

