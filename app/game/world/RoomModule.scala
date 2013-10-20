package game.world

import akka.actor.ActorRef
import game.EventModule
import game.mobile.PlayerModule

/**
 * Defines structures and messages for Room behavior. Rooms are asynchronous
 * EventHandlers that mediate almost all Events that propagate through the world.
 */
trait RoomModule extends EventModule {
  this: PlayerModule with SurfaceModule ⇒

  case object Arrived extends Event

  // All rooms in the game are equipped with the same 4 surrounding surfaces:
  val floor = DoubleSided( Point( 0, 0 ), Point( 200, 0 ) )
  val ceiling = DoubleSided( Point( 0, 200 ), Point( 200, 200 ) )
  val leftWall = Wall( 0, 200, 0 )
  val rightWall = Wall( 200, 200, 0 )

  /**
   * An ActorEventHandler that mediates almost all Events that propagate through the world.
   * Every Room in existence shares the same 4 Surfaces to form a box that contains mobiles.
   * Every
   */
  trait RoomEventHandler extends ActorEventHandler {
    val id: String
    val gravity: BigDecimal
    
    /** This Room's default gravity simply modifies a movement's y-value */
    val gravitate: Adjust = {
      case Moved( p, m ) ⇒ Moved( p, Movement( m.x, m.y + gravity ) )
    }

    // Include the room's default gravity and default walls
    incoming = incoming :+ gravitate
    outgoing = outgoing ::: List( floor, leftWall, rightWall ).flatMap( _.outgoing )

    def default: Handle = {
      case mv: Moved ⇒ emit( mv, forwarding = true )
      case _         ⇒ // yum
    }
  }

  /** Concrete implementation of a RoomEventHandler */
  class Room( override val id: String ) extends RoomEventHandler {
    override val gravity: BigDecimal = -1
    outgoing = outgoing ::: DoubleSided( Point( 0, 0 ), Point( 200, 200 ) ).outgoing
  }
}