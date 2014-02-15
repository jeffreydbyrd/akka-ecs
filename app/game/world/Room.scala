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

  val floor = new game.world.physics.Rect( "test_fixture", 25, 0, 50, 1 )
  val testBox = new game.world.physics.Rect( "text_box", 10, 10, 10, 10 )
  val fixtures = Set( floor, testBox )

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
    for ( f ← fixtures )
      simulation ! Simulation.CreateBlock( f.x, f.y, f.w, f.h )
  }

  override def receive = LoggingReceive {
    eventHandler orElse roomBehavior
  }

}