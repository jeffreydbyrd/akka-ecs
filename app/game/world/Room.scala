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

object Room {
  def props( name: String ) = Props( classOf[ Room ], name )

  // Received Messages
  case class Arrived( mobile: ActorRef, x: Float, y: Float, width: Int, height: Int ) extends Event

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

  val floor = new game.world.physics.Rect( "test_fixture", 25, 5, 50, 1 )
  val fixtures = Set( floor )

  val roomBehavior: Receive = {
    case Game.NewPlayer( client, name ) ⇒
      val plr = context.actorOf( Player.props( name ), name = name )
      subscribers += plr
      plr ! Player.Start( self, client )

    case arr @ Arrived( mobile, x, y, w, h ) ⇒
      simulation ! Simulation.CreateMobile( mobile, x, y, w, h )
      sender ! RoomData( fixtures )

    case mb: Player.MobileBehavior ⇒ simulation ! mb

    case Game.Tick ⇒
      simulation ! Simulation.Step
      emit( Game.Tick )

    case Simulation.Snapshot( mob, x, y ) ⇒ emit( Player.Moved( mob, x, y ) )
  }

  override def preStart() = {
    simulation ! Simulation.CreateBlock( floor.x, floor.y, floor.w, floor.h )
  }

  override def receive = LoggingReceive {
    eventHandler orElse roomBehavior
  }

}