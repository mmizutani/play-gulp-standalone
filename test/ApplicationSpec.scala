import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.twirl.api.Html

@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application Controller" should {

    "render the yeoman gulp-angular index page" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get
      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Gulp Angular")
    }

    "send 404 on a bad request" in new WithApplication {
      val result = route(FakeRequest(GET, "/boum")).get
      status(result) mustEqual NOT_FOUND
    }

    "render the old index page" in new WithApplication {
      val home = route(FakeRequest(GET, "/oldhome")).get
      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Hello Play Framework")
    }

    "render twirl templates in ui/src/views" in new WithApplication {
      val tpl1 = views.html.tpl1("testing view template 1")
      contentAsString(tpl1) must contain("testing view template 1")
      val tpl2 = views.html.tpl2("testing view template 2")(Html(""))
      contentAsString(tpl2) must contain("testing view template 2")
    }

  }

  "GulpAssets Controller" should {

    "render the yeoman gulp-angular index page" in new WithApplication {
      val home = controllers.GulpAssets.index()(FakeRequest())
      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Gulp Angular")
    }

    "prevent directory listing" in new WithApplication {
      val home = route(FakeRequest(GET, "/assets/")).get
      status(home) must equalTo(FORBIDDEN)
    }

  }
}
