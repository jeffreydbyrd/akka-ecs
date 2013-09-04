package game.util.math

import org.specs2.mutable.Specification

class ExpressionModuleSpec
    extends ExpressionModule
    with Specification {

  "gcd should return the greatest common denominator" in {
    gcd( 8, 28 ) === 4
    gcd( 3, 7 ) === 1
  }

  "Int :/ Int should convert to Fraction( Int , Int )" in {
    ( 3 :/ 15 ) === Fraction( 3, 15 )
  }

  "(a :/ b :/ c) should convert to Fraction( a, b*c )" in {
    ( 3 :/ 15 :/ 12 ) === Fraction( 3, 15 * 12 )
  }

  "Fraction.eval" should {
    "convert a Fraction(a, b) to a Double" in {
      ( 3 :/ 15 ).eval === 0.2
    }
    "convert ( (a / b) / c ) to a Double" in {
      ( 15 :/ 3 :/ 2 ).eval === 2.5
    }
  }

  "(3 :/ 4) * (4 :/ 3) should return (12 / 12)" in {
    ( 3 :/ 4 ) * ( 4 :/ 3 ) === 12 :/ 12
  }

  "(3 :/ 4) + (4 :/ 3) should return (25 / 12)" in {
    ( 3 :/ 4 ) + ( 4 :/ 3 ) === 25 :/ 12
  }

  "Fraction.reduce" should {
    "turn (12 :/ 12) into 1 / 1" in {
      ( 12 :/ 12 ).reduce === 1 :/ 1
    }
    "turn (8 :/ 28) into (2 / 7)" in {
      ( 8 :/ 28 ).reduce === 2 :/ 7
    }
  }

  "Sqrt(7)" should {
    val test = Sqrt( 7 )

    "* Sqrt(7) == Sqrt(49)" in {
      test * Sqrt( 7 ) == Sqrt( 49 )
    }

    "/ Sqrt(7) == Sqrt(1)" in {
      test / Sqrt( 7 ) == Sqrt( 1 )
    }

    "* 7 == Sqrt(343)" in {
      test * 7 == Sqrt( 343 )
    }

    "+ 7 == Composed(Sqrt(7), 7)" in {
      test + 7 == Composed( Sqrt( 7 ), 7 )
    }

    "- 7 == Composed(Sqrt(7), -7)" in {
      test - 7 == Composed( Sqrt( 7 ), -7 )
    }

  }

  "Composition test:" in {
    val sq = Sqrt( 16 )
    val fr = 25
    ( fr + sq ) == Composed( 25, Sqrt( 16 ) )
  }

  "Comprehensive test:" in {
    val dx = 4
    val dy = 3
    val len = Sqrt( ( 4 * 4 ) + ( 3 * 3 ) )
    val k = ( len / 3 )
    ( dx / k ) == Sqrt( 16 :/ ( 25 :/ 9 ) )

  }

}