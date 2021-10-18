import cats.effect.IO

import munit._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._
import org.http4s.syntax.all._

class Http4sDemoSuite extends Http4sHttpRoutesSuite {

  override val routes: HttpRoutes[IO] = HttpRoutes.of {
    case GET -> Root / "hello"        => Ok("Hi")
    case GET -> Root / "hello" / name => Ok(s"Hi $name")
  }

  test(GET(uri"/hello" / "gutiory")) { response =>
    assertIO(response.as[String], "Hi gutiory")
  }

}
