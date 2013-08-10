package game

import akka.actor.ActorRef

trait RoomModule extends EventModule {
  this: PlayerModule ⇒

  case class Arrived() extends Event
  case class Moved( ar:ActorRef, dist: Int ) extends Event

  class Room( override val id: String ) extends EHRoom

  trait EHRoom extends EventHandler {
    val id: String

    def default: Handle = {
      case Arrived()           ⇒ subscribers = subscribers :+ sender
      case MoveAttempt( dist ) ⇒ this emit Moved( sender, dist )
      case _                   ⇒
    }
  }
}