package game.world

import org.specs2.mutable.Specification
import game.mobile.PlayerModule
import game.GameModule
import akka.actor.ActorSystem

class RoomModuleSpec
    extends RoomModule
    with PlayerModule
    with GameModule
    with Specification {
  implicit def system: akka.actor.ActorSystem = ActorSystem("RoomModuleSpec")
  val GAME: akka.actor.ActorRef = null
  
  
}