package game.world

import akka.actor.ActorRef
import game.EventModule
import game.mobile.PlayerModule

trait RoomModule extends EventModule {
  this: PlayerModule ⇒

  case object Arrived extends Event
  case class Moved( ar: ActorRef, dist: Int ) extends Event

  class Room( override val id: String ) extends EHRoom

  trait EHRoom extends EventHandler {
    val id: String

    def default: Handle = {
      case MoveAttempt( dist ) ⇒ this emit Moved( sender, dist )
      case _                   ⇒
    }
  }
}