package controllers

import javax.inject._
import java.io.File

import play.api._
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.collection.JavaConversions._

@Singleton
class GulpAssets @Inject() (env: Environment, conf: Configuration) extends Controller {

  private lazy val logger = Logger(getClass)

  /**
    * Serve the index page (ui/{src,dist}/index.html) built by Gulp tasks
    *
    * @return Index HTML file
    */
  def index = Action.async { request =>
    if (request.path.endsWith("/")) {
      at("index.html").apply(request)
    } else {
      Future(Redirect(request.path + "/"))
    }
  }

  // List of UI directories from which static assets are served in the development mode
  val runtimeDirs = conf.getStringList("gulp.devDirs")
  val basePaths: List[java.io.File] = runtimeDirs match {
    case Some(dirs) => dirs.map(env.getFile).toList
    // If "gulp.devDirs" is not specified in conf/application.conf
    case _ => List(
      env.getFile("ui/dist"),
      env.getFile("ui/src"),
      env.getFile("ui"),
      env.getFile("public")
    )
  }

  /**
    * Asset Handler for development and test modes
    *
    * @param file Path and file name of the static asset served to the client in the development mode
    * @return Static asset file
    */
  private[controllers] def devAssetHandler(file: String): Action[AnyContent] = Action { request =>
    // Generates a non-strict list of the full paths
    val targetPaths = basePaths.view map {
      new File(_, file)
    }

    // Generates responses returning the file in the dev and test modes only (not in the production mode)
    val responses = targetPaths filter { file =>
      file.exists()
    } map { file =>
      if (file.isFile) {
        logger.info(s"Serving $file")
        Ok.sendFile(file, inline = true).withHeaders(CACHE_CONTROL -> "no-store")
      } else {
        Forbidden(views.html.defaultpages.unauthorized())
      }
    }

    // Returns the first valid path if valid or NotFound otherwise
    responses.headOption getOrElse NotFound("404 - Page not found error\n" + request.path)
  }

  /**
    * Asset handler for production mode
    *
    * Static asset files (JavaScript, CSS, images, etc.) in app/assets, public and ui/dist folders
    * are all placed in the /public folder of the classpath in production mode.
    *
    * @param file Path and file name of the static asset served to the client in the production mode
    * @return Static asset file
    */
  private[controllers] def prodAssetHandler(file: String): Action[AnyContent] = Assets.at("/public", file)


  lazy val atHandler: String => Action[AnyContent] =
    env.mode match {
      case Mode.Prod => prodAssetHandler(_: String)
      case _ => devAssetHandler(_: String)
    }

  /**
    * Asset handler for development/test/production modes
    *
    * @param file Path and file name of the static asset served to the client in each mode
    * @return Static asset file
    */
  def at(file: String): Action[AnyContent] = atHandler(file)

}