package game.world

import org.specs2.mutable.Specification
import game.mobile.PlayerModule
import game.GameModule
import akka.actor.ActorSystem
import game.EventModule
import game.mobile.MobileModule
import game.communications.ConnectionModule
import game.util.logging.LoggingModule

class RoomModuleSpec
    extends RoomModule
    with Specification
    with EventModule
    with GameModule
    with SurfaceModule
    with PlayerModule
    with MobileModule
    with ConnectionModule
    with LoggingModule {
  implicit def system: akka.actor.ActorSystem = ActorSystem( "RoomModuleSpec" )
  val game: akka.actor.ActorRef = null
  val timeout = null

}