package game.world

import akka.actor.ActorRef
import game.EventModule
import game.mobile.PlayerModule

trait RoomModule extends EventModule {
  this: PlayerModule with SurfaceModule ⇒

  case object Arrived extends Event
  case class Moved( ar: ActorRef, p: Position, m: Movement ) extends Event

  // All rooms in the game are equipped with the same 4 surrounding surfaces:
  val ceiling = DoubleSided( 100, 200, 200, Flat )
  val floor = DoubleSided( 100, 0, 200, Flat )
  val leftWall = Wall( 0, 100, 200 )
  val rightWall = Wall( 200, 100, 200 )

  trait GenericRoom {
    val id: String
  }

  trait EHRoom extends GenericRoom with EventHandler {
    adjusts = adjusts ::: List( ceiling, floor, leftWall, rightWall ).flatMap( _.getAdjusts )

    def default: Handle = {
      case MoveAttempt( p, m ) ⇒ this emit Moved( sender, p, Movement( m.x, m.y - 1 ) )
      case _                   ⇒
    }
  }

  class Room( override val id: String ) extends EHRoom
}