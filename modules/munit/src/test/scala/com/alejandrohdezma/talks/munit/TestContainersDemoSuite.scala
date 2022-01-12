/*
 * Copyright 2020-2022 Alejandro Hernández <https://github.com/alejandrohdezma>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alejandrohdezma.talks.munit

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
