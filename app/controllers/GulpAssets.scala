package controllers

import java.io.File
import javax.inject.Singleton
import play.api._
import play.api.mvc.{AnyContent, Action}
import play.api.Play.current
import play.api.http.DefaultHttpErrorHandler
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.collection.JavaConverters._

object GulpAssets extends controllers.Assets(DefaultHttpErrorHandler) {

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
  val runtimeDevDirs: Option[java.util.List[String]] = Play.configuration.getStringList("yeoman.devDirs")
  // A directory in higher priority comes first.
  val basePaths: List[java.io.File] = runtimeDevDirs match {
    case Some(dirs:List[String]) => dirs.map(Play.application.getFile _)
    case _ => List(
      Play.application.getFile("ui/.tmp/serve"),
      Play.application.getFile("ui/src"),
      Play.application.getFile("ui")
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


  lazy val atHandler: String => Action[AnyContent] = if (Play.isProd) prodAssetHandler(_: String) else devAssetHandler(_: String)

  /**
   * Asset handler for development/test/production modes
   *
   * @param file Path and file name of the static asset served to the client in each mode
   * @return Static asset file
   */
  def at(file: String): Action[AnyContent] = atHandler(file)

}

@Singleton
class GulpAssets extends controllers.Assets(DefaultHttpErrorHandler) {
  // These two static methods are used in the routes file and twirl scala.html template files.
  def index = GulpAssets.index
  def at(file: String) = GulpAssets.at(file: String)
}