package com.alejandrohdezma.talks.munit

import munit.ScalaCheckSuite
import org.scalacheck.Prop._

class PropertyBasedSuite extends ScalaCheckSuite {

  property("addition is commutative") {
    forAll { (n1: Int, n2: Int) =>
      assertEquals(n1 + n2, n2 + n1)
    }
  }

}
