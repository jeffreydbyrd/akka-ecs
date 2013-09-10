package game.world

import akka.actor.ActorRef
import game.EventModule
import game.mobile.PlayerModule

trait RoomModule extends EventModule {
  this: PlayerModule with SurfaceModule ⇒

  case object Arrived extends Event
  case class Moved( ar: ActorRef, p: Position, m: Movement ) extends Event

  // All rooms in the game are equipped with the same 4 surrounding surfaces:
  val floor = DoubleSided( Point( 0, 0 ), Point( 200, 200 ) )
  val ceiling = DoubleSided( Point( 0, 200 ), Point( 200, 200 ) )
  val leftWall = Wall( 0, 200, 0 )
  val rightWall = Wall( 200, 200, 0 )

  trait GenericRoom {
    val id: String
    val gravity = -1
  }

  trait EHRoom extends GenericRoom with EventHandler {
    outgoing = outgoing ::: List( floor, leftWall, rightWall ).flatMap( _.outgoing )

    def default: Handle = {
      case MoveAttempt( p, m ) ⇒ this emit Moved( sender, p, Movement( m.x, m.y + gravity ) )
      case _                   ⇒
    }
  }

  class Room( override val id: String ) extends EHRoom {
//    val platform = DoubleSided( Point( 20, 6 ), Point( 30, 16 ) )
//    adjusts = adjusts ::: platform.getAdjusts
  }
}