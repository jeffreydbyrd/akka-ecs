package game.util.math

import org.specs2.mutable.Specification

class LineModuleSpec extends LineModule with Specification {
  "Slope.m" should {
    "return BigDec if the slope is a Slant" in {
      Slant( 2, 2 ).m === 1.0
    }

    "return 0.0 if the slope is a Flat" in {
      Flat.m === 0.0
    }

    "throw UndefinedSlopeException if Undefined" in {
      Undefined.m must throwA[ UndefinedSlopeException ]
    }
  }

  "The Slope(dx, dy) companion object" should {
    "return a Undefined Slope when dx = 0" in {
      Slope( 0, 0 ) === Undefined
    }

    "return a Flat Slope when dy = 0" in {
      Slope( 1, 0 ) === Flat
    }

    "return a Slant when dx and dy are nonzero" in {
      Slope( 1, -1 ) === Slant( 1, -1 )
    }
  }

  "Intersection" should {
    "be able to represent the intersection of 2 slanted lines" in {
      val l1 = Vector( Point( 0, 0 ), Point( 10, 10 ) )
      val l2 = Vector( Point( 0, 10 ), Point( 10, 0 ) )
      val int = new Intersection( l1, l2 )
      int.x === 5
      int.y === 5
    }

    "be able to represent the intersection of a slanted line and vertical line" in {
      val slanted = Vector( Point( 0, 0 ), Point( 10, 10 ) )
      val vertical = Vector( Point( 5, 0 ), Point( 5, 10 ) )
      val int1 = new Intersection( slanted, vertical )
      int1.x === 5
      int1.y === 5

      val int2 = new Intersection( vertical, slanted )
      int2.x === 5
      int2.y === 5
    }

    "throw an IllegalArgumentException if both lines are parallel" in {
      val l1 = Vector( Point( 0, 0 ), Point( 0, 10 ) )
      val l2 = Vector( Point( 1, 0 ), Point( 1, 10 ) )
      ( new Intersection( l1, l2 ) ) must throwA[ IllegalArgumentException ]
    }
  }
}