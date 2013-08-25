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

  "Floor.isLanding( (x, y), yspeed )" should {
    "return true when y + yspeed crosses the Floor" in {
      DoubleSided( 3, 1, 4, Flat ).isLanding( ( 3, 2 ), -2 ) === true
    }

    "return false when y + yspeed doesn't cross the Floor" in {
      DoubleSided( 3, 1, 4, Flat ).isLanding( ( 3, 4 ), -2 ) === false
    }

    "return true when y + yspeed crosses a Slanted floor" in {
      DoubleSided( 3, 1, 4, Slant( 1, 1 ) ).isLanding( ( 4, 4 ), -2 ) === true
    }

    "return false when y + yspeed doesn't cross a Slanted floor" in {
      DoubleSided( 3, 1, 4, Slant( 1, 1 ) ).isLanding( ( 4, 4 ), -1 ) === false
    }
  }

  "Floor.inBounds(p)" should {
    "return true when 'p' overlaps with the x-length (width) of the floor" in {
      DoubleSided( 3, 1, 4, Slant( 1, 1 ) ).inBounds( Position( 5, 4 ) ) === true
    }
    
    "return false when 'p' doesn't overlap with the x-length (width) of the floor" in {
      DoubleSided( 3, 1, 4, Slant( 1, 1 ) ).inBounds( Position( 6, 4 ) ) === false
    }
  }

}