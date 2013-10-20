package game.world

import akka.actor.ActorRef
import game.EventModule
import game.mobile.PlayerModule

/**
 * Defines structures and messages for Room behavior. Rooms are asynchronous
 * EventHandlers.
 */
trait RoomModule extends EventModule {
  this: PlayerModule with SurfaceModule ⇒

  case object Arrived extends Event
  case class Moved( p: Position, m: Movement ) extends Event

  // All rooms in the game are equipped with the same 4 surrounding surfaces:
  val floor = DoubleSided( Point( 0, 0 ), Point( 200, 200 ) )
  val ceiling = DoubleSided( Point( 0, 200 ), Point( 200, 200 ) )
  val leftWall = Wall( 0, 200, 0 )
  val rightWall = Wall( 200, 200, 0 )

  trait EHRoom extends ActorEventHandler {
    val id: String
    val gravity = -1
    val gravitate: Adjust = {
      case Moved( p, m ) ⇒ Moved( p, Movement( m.x, m.y + gravity ) )
    }

    outgoing = outgoing ::: gravitate :: List( floor, leftWall, rightWall ).flatMap( _.outgoing )

    def default: Handle = {
      case mv: Moved ⇒ emit( mv, forwarding = true )
      case _ ⇒ // yum
    }
  }

  class Room( override val id: String ) extends EHRoom
}