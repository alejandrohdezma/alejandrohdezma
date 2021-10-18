import com.dimafeng.testcontainers.GenericContainer

import com.dimafeng.testcontainers.munit.TestContainerForAll
import munit.FunSuite
import org.testcontainers.containers.wait.strategy.Wait

class TestContainersDemoSuite extends FunSuite with TestContainerForAll {

  override val containerDef = DummyHttpContainer.Def()

  test("Ping-pong")(withContainers { container =>
    val url = s"http://${container.host}:${container.mappedPort(80)}/ping"

    val result = scala.io.Source.fromURL(url).mkString // scalafix:ok

    assertEquals(result, "pong")
  })

}

final case class DummyHttpContainer(underlying: GenericContainer) extends GenericContainer(underlying)

object DummyHttpContainer {

  final case class Def()
      extends GenericContainer.Def[DummyHttpContainer](
        new DummyHttpContainer(
          GenericContainer(
            dockerImage = "briceburg/ping-pong",
            exposedPorts = Seq(80),
            waitStrategy = Wait.forHttp("/ping")
          )
        )
      )

}
