package game.world

import org.specs2.mutable.Specification
import game.mobile.MobileModule
import game.EventModule
import game.mobile.PlayerModule
import game.ConnectionModule

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

}