package com.alejandrohdezma.talks.munit

import cats.effect.IO

import munit.CatsEffectSuite

class CatsEffectDemoSuite extends CatsEffectSuite {

  test("alternatively, asertions can be written via assertIO") {
    assertIO(IO(42), 42)
  }

}
