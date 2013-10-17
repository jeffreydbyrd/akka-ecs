package game.util.math

import org.specs2.mutable.Specification

class LineModuleSpec extends LineModule with Specification {
  "Slope.m" should {
    "return Double if the slope is a Slant" in {
      Slant( 2, 2 ).m === 1.0
    }

    "return 0.0 if the slope is a Flat" in {
      Flat.m === 0.0
    }

    "throw UndefinedSlopeException if Undefined" in {
      Undefined.m must throwA[ UndefinedSlopeException ]
    }
  }
}