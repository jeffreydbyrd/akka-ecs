package game.world

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.Game
import game.events.Event
import game.events.EventHandler
import game.mobile.Player
import game.world.physics.Fixture
import game.world.physics.Rect

object Room {
  def props( name: String ) = Props( classOf[ Room ], name )

  // Received Messages
  case class Arrived( mobile: ActorRef, dims: Rect ) extends Event

  // Sent Messages
  case class RoomData( fixtures: Iterable[ Fixture ] )
}

/**
 * An ActorEventHandler that mediates almost all Events that propagate through the world.
 * Every Room in existence shares the same 4 Surfaces to form a box that contains mobiles.
 */
class Room( val id: String ) extends EventHandler {
  import Room._

  val simulation = context.actorOf( Simulation.props(), name = "simulation" )

  val floor = new game.world.physics.Rect( "floor", 25, 1, 50, 1 )
  val leftWall = new game.world.physics.Rect( "left_wall", 1, 25, 1, 50 )
  val rightWall = new game.world.physics.Rect( "right_wall", 49, 25, 1, 50 )
  val top = new game.world.physics.Rect( "top", 25, 49, 50, 1 )

  val fixtures = Set( floor, leftWall, rightWall, top )

  val roomBehavior: Receive = {
    case arr @ Arrived( mobile, Rect( _, x, y, w, h ) ) ⇒
      subscribers += mobile
      simulation ! Simulation.CreateMobile( mobile, x, y, w, h )
      sender ! RoomData( fixtures )
      emit( arr )

    case q @ Player.Quit( mob ) ⇒
      subscribers -= mob
      context.parent ! q
      simulation ! q

    case mb: Player.MobileBehavior ⇒ simulation ! mb

    case Game.Tick ⇒
      simulation ! Simulation.Step
      emit( Game.Tick )

    case Simulation.Snapshot( mob, x, y ) ⇒ emit( Player.Moved( mob, x, y ) )
  }

  override def preStart() = {
    for ( f ← fixtures )
      simulation ! Simulation.CreateBlock( f.x, f.y, f.w, f.h )
  }

  override def receive = LoggingReceive {
    eventHandler orElse roomBehavior
  }

}