package game.world

import akka.actor.ActorRef
import game.EventModule
import game.mobile.PlayerModule

trait RoomModule extends EventModule {
  this: PlayerModule with SurfaceModule ⇒

  case object Arrived extends Event
  case class Moved( ar: ActorRef, p: Position, m: Moving ) extends Event

  // All rooms in the game are equipped with the same 4 surrounding surfaces:
  val ceiling = DoubleSided( 100, 200, 200, Flat )
  val floor = DoubleSided( 100, 0, 200, Flat )
  val leftWall = Wall( 0, 100, 200 )
  val rightWall = Wall( 200, 100, 200 )

  trait GenericRoom {
    val id: String

    val gravity: Adjust = {
      case Moved( ar, p, m: Falling ) ⇒
        Moved( ar, p, Falling( m.y - 1 ) )
    }
  }

  trait EHRoom extends GenericRoom with EventHandler {
    adjusts = adjusts :+ gravity
    adjusts = adjusts ::: List( ceiling, floor, leftWall, rightWall ).flatMap( _.getAdjusts )

    def default: Handle = {
      case MoveAttempt( p, m ) ⇒ this emit Moved( sender, p, m )
      case _                   ⇒
    }
  }

  class Room( override val id: String ) extends EHRoom
}