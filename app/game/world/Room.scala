package game.world

import scala.math.BigDecimal.int2bigDecimal

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.Game
import game.events.Adjust
import game.events.Event
import game.events.EventHandler
import game.mobile.Mobile.Moved
import game.mobile.Movement
import game.mobile.Player
import game.util.math.Point

object Room {
  def props( name: String ) = Props( classOf[ Room ], name )

  // Received Messages
  case object Arrived extends Event

  // Sent Messages
  case class RoomData( children: Iterable[ ActorRef ] ) extends Event

  // All rooms in the game are equipped with the same 4 surrounding surfaces:
  val floor = DoubleSided( Point( 0, 0 ), Point( 200, 0 ) )
  val ceiling = DoubleSided( Point( 0, 200 ), Point( 200, 200 ) )
  val leftWall = Wall( 0, 200, 0 )
  val rightWall = Wall( 200, 200, 0 )

  val gravity: BigDecimal = -1

  // put a big slanted surface through the middle of the room:
  val slanted = DoubleSided( Point( 0, 0 ), Point( 200, 200 ) )
}

/**
 * An ActorEventHandler that mediates almost all Events that propagate through the world.
 * Every Room in existence shares the same 4 Surfaces to form a box that contains mobiles.
 */
class Room( val id: String ) extends EventHandler {
  import Room._

  /** This Room's default gravity simply modifies a movement's y-value */
  val gravitate: Adjust = {
    case Moved( ar, p, m ) ⇒ Moved( ar, p, Movement( m.x, m.y + gravity ) )
  }

  // Include the room's default gravity and default floors
  adjusters = adjusters + gravitate + floor.onCollision + slanted.onCollision

  val roomBehavior: Receive = {
    // create a new player, tell him to Start
    case Game.NewPlayer( client, name ) ⇒
      val plr = context.actorOf( Player.props( name ), name = name )
      subscribers += plr
      plr ! Player.Start( self, client )
    case Arrived   ⇒ sender ! RoomData( context.children )
    case mv: Moved ⇒ emit( mv )
    case Game.Tick ⇒ emit( Game.Tick )
  }

  override def receive = LoggingReceive {
    eventHandler orElse roomBehavior
  }

}