package game.world

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.Game
import game.events.Event
import game.events.EventHandler
import game.mobile.Player

object Room {
  def props( name: String ) = Props( classOf[ Room ], name )

  // Received Messages
  case class Arrived( mobile: ActorRef, x: Float, y: Float, width: Int, height: Int ) extends Event

  // Sent Messages
  case class RoomData( subscribers: Iterable[ ActorRef ] ) extends Event
}

/**
 * An ActorEventHandler that mediates almost all Events that propagate through the world.
 * Every Room in existence shares the same 4 Surfaces to form a box that contains mobiles.
 */
class Room( val id: String ) extends EventHandler {
  import Room._

  val simulation = context.actorOf( PhysicsSimulation.props(), name = "simulation" )

  val roomBehavior: Receive = {
    case Game.NewPlayer( client, name ) ⇒
      val plr = context.actorOf( Player.props( name ), name = name )
      subscribers += plr
      plr ! Player.Start( self, client )

    case Arrived( mobile, x, y, w, h ) ⇒
      simulation ! PhysicsSimulation.AddMobile( mobile, x, y, w, h )
      sender ! RoomData( subscribers )

    case sm: Player.Walking ⇒ simulation ! sm
    case sm: Player.Standing ⇒ simulation ! sm

    case Game.Tick ⇒
      simulation ! PhysicsSimulation.Step
      emit( Game.Tick )

    case PhysicsSimulation.Snapshot( mob, x, y ) ⇒ emit( Player.Moved( mob, x, y ) )
  }

  override def preStart() = {
    simulation ! PhysicsSimulation.AddBlock( 100, 10, 200, 1 )
  }

  override def receive = LoggingReceive {
    eventHandler orElse roomBehavior
  }

}