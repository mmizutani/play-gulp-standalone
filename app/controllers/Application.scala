package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.{Action, Controller}
import play.api.routing.{ JavaScriptReverseRouter, Router, JavaScriptReverseRoute }
import play.api.Play.current
import play.twirl.api.Html
import javax.inject.{ Inject, Singleton }

@Singleton
class Application @Inject() (router: Router) extends Controller {

  /**
   * Returns ui/src/index.html in dev/test mode and ui/dist/index.html in production mode
   */
  def index = GulpAssets.index
	
  def oldhome = Action {
    Ok(views.html.index("Play Framework"))
  }

  // (Optional)
  // Enable `withTemplates` setting in build.sbt and place Twirl scala.html templates in ui/src/app/views folders
//  def tpl1 = Action {
//    Ok(views.html.tpl1("Compiled from a scala template!"))
//  }
//
//  def tpl2 = Action {
//    Ok(views.html.tpl2("Scala template in Angular")
//      (Html("<p>This is a play scala html view compiled by the twirl template engine.</p>"))
//    )
//  }

  // Collects JavaScript routes using reflection
  val routeCache: Array[JavaScriptReverseRoute] = {
    val jsRoutesClass = classOf[controllers.routes.javascript]
    for {
      controller <- jsRoutesClass.getFields.map(_.get(null))
      method <- controller.getClass.getDeclaredMethods if method.getReturnType == classOf[JavaScriptReverseRoute]
    } yield method.invoke(controller).asInstanceOf[JavaScriptReverseRoute]
  }

  /**
   * Returns the JavaScript router that the client can use for "type-safe" routes.
   * @param varName The name of the global variable, defaults to `jsRoutes`
   */
  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(JavaScriptReverseRouter(varName)(routeCache: _*)).as(JAVASCRIPT)
  }

  val herokuDemo = true

  /**
   * Returns a list of all the HTTP action routes for easier debugging
   */
  def routes = Action { request =>
    if (Play.isProd && !herokuDemo)
      NotFound
    else
      Ok(views.html.devRoutes(request.method, request.uri, Some(router)))
  }

}
