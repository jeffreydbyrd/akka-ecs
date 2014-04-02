package game.world

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.core.Game
import game.communications.proxies.ClientProxy
import game.world.physics.Fixture
import game.world.physics.Rect
import game.world.physics.Simulation
import akka.actor.Actor

object Room {
  def props( name: String ) = Props( classOf[ Room ], name )

  // Received Messages
  case class Arrived( mobile: ActorRef, dims: Rect )

  // Sent Messages
  case class RoomData( fixtures: Iterable[ Fixture ] )
}

class Room( val id: String ) extends Actor {
  import Room._

  val simulation = context.actorOf( Simulation.props( 0, -25 ), name = "simulation" )

  val floor = new game.world.physics.Rect( "floor", 25, 1, 50, 1 )
  val leftWall = new game.world.physics.Rect( "left_wall", 1, 25, 1, 50 )
  val rightWall = new game.world.physics.Rect( "right_wall", 49, 25, 1, 50 )
  val top = new game.world.physics.Rect( "top", 25, 49, 50, 1 )

  val fixtures = Set( floor, leftWall, rightWall, top )
  var proxies: Set[ ActorRef ] = Set()

  def receive: Receive = LoggingReceive {
    case arr @ Arrived( mobile, Rect( _, x, y, w, h ) ) ⇒
      proxies += mobile
      simulation ! Simulation.CreateMobile( mobile, x, y, w, h )
      mobile ! RoomData( fixtures )
      for ( p ← proxies ) p ! arr

    case q @ ClientProxy.Quit( mob ) ⇒
      proxies -= mob
      context.parent ! q
      simulation ! q

    case evt: ClientProxy.Move ⇒ simulation ! evt

    case Game.Tick ⇒
      simulation ! Simulation.Step
      for ( p ← proxies ) p ! Game.Tick

    case snap: Simulation.Snapshot ⇒ for ( p ← proxies ) p ! snap
  }

  override def preStart() = {
    for ( f ← fixtures )
      simulation ! Simulation.CreateBlock( f.x, f.y, f.w, f.h )
  }
}