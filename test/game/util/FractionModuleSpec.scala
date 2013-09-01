package game.util

import org.specs2.mutable.Specification

class FractionModuleSpec
    extends FractionModule
    with Specification {

  "gcd should return the greatest common denominator" in {
    gcd( 8, 28 ) === 4
  }

  ":/" should {
    "convert (Int :/ Int) to Frac( FracInt , FracInt )" in {
      ( 3 :/ 15 ) === Frac( FracInt( 3 ), FracInt( 15 ) )
    }

    "convert (Int :/ Int :/ Int) to Frac( Frac( FracInt, FracInt ), FracInt )" in {
      ( 3 :/ 15 :/ 12 ) === Frac( Frac( 3, 15 ), 12 )
    }
  }

  "Fraction.toDouble" should {
    "convert a Frac(FracInt, FracInt) to a Double" in {
      ( 3 :/ 15 ).toDouble === 0.2
    }
    "convert a Frac( Frac( FracInt, FracInt ), FracInt ) to a Double" in {
      ( 15 :/ 3 :/ 2 ).toDouble === 2.5
    }
  }

}