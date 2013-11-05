package game.world

import scala.concurrent.ExecutionContext.Implicits.global
import scala.math.BigDecimal.int2bigDecimal
import akka.actor.Props
import akka.pattern.ask
import game.EventModule
import game.GameModule
import game.mobile.PlayerModule
import akka.actor.ActorRef
import game.mobile.MobileModule

/**
 * Defines structures and messages for Room behavior. Rooms are asynchronous
 * EventHandlers that mediate almost all Events that propagate through the world.
 */
trait RoomModule {
  this: EventModule with SurfaceModule with MobileModule with PlayerModule with GameModule ⇒

  case object Arrived extends Event

  case class RoomData( children: Iterable[ ActorRef ] )

  // All rooms in the game are equipped with the same 4 surrounding surfaces:
  val floor = DoubleSided( Point( 0, 0 ), Point( 200, 0 ) )
  val ceiling = DoubleSided( Point( 0, 200 ), Point( 200, 200 ) )
  val leftWall = Wall( 0, 200, 0 )
  val rightWall = Wall( 200, 200, 0 )

  /**
   * An ActorEventHandler that mediates almost all Events that propagate through the world.
   * Every Room in existence shares the same 4 Surfaces to form a box that contains mobiles.
   */
  trait RoomEventHandler extends ActorEventHandler {
    val id: String
    val gravity: BigDecimal

    /** This Room's default gravity simply modifies a movement's y-value */
    val gravitate: Adjust = {
      case Moved( p, m ) ⇒ Moved( p, Movement( m.x, m.y + gravity ) )
    }

    def newPlayer( name: String ) = context.actorOf( Props( new Player( name ) ), name = name )

    // Include the room's default gravity and default walls
    incoming = incoming :+ gravitate
    outgoing = outgoing ::: List( floor, leftWall, rightWall ).flatMap( _.outgoing )

    def listen: Receive = {
      // create a new player, tell him to Start
      case AddPlayer( name ) ⇒
        logger.info( s"received AddPlayer( $name ) from game actor" )
        newPlayer( name ) forward Start
    }
    override def receive = listen orElse super.receive

    def default: Handle = {
      case Arrived   ⇒ sender ! RoomData( context.children )
      case mv: Moved ⇒ emit( mv, forwarding = true )
      case _         ⇒ // yum
    }
  }

  /** Concrete implementation of a RoomEventHandler */
  class Room( override val id: String ) extends RoomEventHandler {
    override val gravity: BigDecimal = -1

    // put a big slanted surface through the middle of the room:
    outgoing = outgoing ::: DoubleSided( Point( 0, 0 ), Point( 200, 200 ) ).outgoing
  }

}