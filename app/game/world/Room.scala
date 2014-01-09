package game.world

import scala.math.BigDecimal.int2bigDecimal
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import game.Game.AddPlayer
import game.mobile.Mobile.Moved
import game.mobile.Player
import game.util.math.Point
import game.events.Event
import game.events.EventHandler
import game.events.Adjust
import game.events.Handle
import game.mobile.Movement

object Room {
  case object Arrived extends Event

  case class RoomData( children: Iterable[ ActorRef ] ) extends Event
  // All rooms in the game are equipped with the same 4 surrounding surfaces:
  val floor = DoubleSided( Point( 0, 0 ), Point( 200, 0 ) )
  val ceiling = DoubleSided( Point( 0, 200 ), Point( 200, 200 ) )
  val leftWall = Wall( 0, 200, 0 )
  val rightWall = Wall( 200, 200, 0 )
}

/**
 * An ActorEventHandler that mediates almost all Events that propagate through the world.
 * Every Room in existence shares the same 4 Surfaces to form a box that contains mobiles.
 */
class Room( val id: String ) extends EventHandler {
  import Room._

  val gravity: BigDecimal = -1

  // put a big slanted surface through the middle of the room:
  outgoing = outgoing ::: DoubleSided( Point( 0, 0 ), Point( 200, 200 ) ).outgoing

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
    case AddPlayer( name ) ⇒ newPlayer( name ) forward Player.Start
  }
  override def receive = listen orElse super.receive

  def default: Handle = {
    case Arrived   ⇒ sender ! RoomData( context.children )
    case mv: Moved ⇒ emit( mv, forwarding = true )
    case _         ⇒ // yum
  }
}