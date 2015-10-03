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
      contentAsString(home) must contain ("Gulp AngularJS")
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

  }

  "GulpAssets Controller" should {

    "render the yeoman gulp-angular index page" in new WithApplication {
      val home = controllers.GulpAssets.index()(FakeRequest())
      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Gulp AngularJS")
    }

    "prevent directory listing" in new WithApplication {
      val home = route(FakeRequest(GET, "/assets/")).get
      status(home) must equalTo(FORBIDDEN)
    }

  }
}
