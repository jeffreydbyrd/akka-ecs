package game.world

import org.specs2.mutable.Specification

import game.ConnectionModule
import game.EventModule
import game.mobile.MobileModule
import game.mobile.PlayerModule

class SurfaceModuleSpec
    extends SurfaceModule
    with EventModule
    with RoomModule
    with PlayerModule
    with ConnectionModule
    with MobileModule
    with Specification { // that's a lot of mixins

  override val system = null

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

  "Floor.isLanding( Position, Movement )" should {
    "return true when the Mobile's vector crosses a Flat Floor" in {
      DoubleSided( Point( 0, 1 ), Point( 10, 1 ) ).isLanding( Position( 3, 4 ), Movement( 0, -2 ) ) === true
    }

    "return false when Mobile's vector doesn't cross a Flat Floor" in {
      DoubleSided( Point( 0, 1 ), Point( 10, 1 ) ).isLanding( Position( 3, 10 ), Movement( 0, -2 ) ) === false
    }

    "return true when Mobile's vector crosses a Slanted floor" in {
      DoubleSided( Point( 3, 2 ), Point( 5, 4 ) ).isLanding( Position( 4, 4 ), Movement( 0, -5 ) ) === true
    }

    "return false when Mobile's vector doesn't cross a Slanted floor" in {
      DoubleSided( Point( 3, 2 ), Point( 5, 4 ) ).isLanding( Position( 4, 10 ), Movement( 0, -1 ) ) === false
    }
  }

}