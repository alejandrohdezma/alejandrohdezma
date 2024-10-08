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

import cats.effect.IO
import cats.effect.SyncIO

import munit._
import org.http4s._
import org.http4s.client.Client

class Http4sDemoSuite extends Http4sSuite {

  override def http4sMUnitClientFixture: SyncIO[FunFixture[Client[IO]]] = HttpRoutes
    .of[IO] {
      case GET -> Root / "hello"        => Ok("Hi")
      case GET -> Root / "hello" / name => Ok(s"Hi $name")
    }
    .orFail
    .asFixture

  test(GET(uri"/hello" / "gutiory")) { response =>
    assertIO(response.as[String], "Hi gutiory")
  }

}
