package game.world

import org.specs2.mutable.Specification
import game.mobile.MobileModule
import game.EventModule
import game.mobile.PlayerModule
import game.ConnectionModule

trait SurfaceModuleSpec
    extends SurfaceModule
    with EventModule
    with RoomModule
    with PlayerModule
    with ConnectionModule
    with MobileModule
    with Specification { // that's a lot of mixins
  
  
  
}