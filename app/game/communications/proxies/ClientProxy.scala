package game.mobile

import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.Game
import game.communications.commands._
import game.communications.connection.PlayActorConnection
import game.world.Room
import game.world.physics.Rect
import game.world.physics.Simulation
import akka.actor.Actor
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Concurrent.Channel
import game.world.physics.Fixture
import game.Game.Connect

object ClientProxy {
  def props( game: ActorRef, inputSync: ActorRef ) = Props( classOf[ ClientProxy ], game, inputSync )

  // Sent Messages
  trait Command
  trait Direction
  case object Left extends Direction
  case object Right extends Direction
  case class Move( mobile: ActorRef, dir: Direction ) extends Command
  case class Halt( mobile: ActorRef, dir: Direction ) extends Command
  case object Jump extends Command
  case class Quit( mob: ActorRef )
  case class PlayerData( mobile: ActorRef, dims: Rect )
}

/**
 * Represents a Client (remote or local... doesn't matter). The ClientProxy doesn't hold any
 * state about its character. It merely provides commands and consumes updates that it delivers
 * to the real Client. The ClientProxy attaches to a Room
 */
class ClientProxy( val game: ActorRef, val inputComponent: ActorRef ) extends Actor {
  import ClientProxy._

  var connection: ActorRef = _

  def updateRoomData( fixtures: Iterable[ Fixture ] ) =
    for ( f ← fixtures ) f match {
      case r: Rect ⇒ connection ! CreateRect( r.id, r, true )
    }

  def createMobile( mobile: ActorRef, rect: Rect ) =
    connection ! CreateRect( mobile.path.toString, rect, true )

  def updateMobilePositions( positions: Map[ ActorRef, ( Float, Float ) ] ) = {
    val ps = positions.map { case ( ref, pos ) ⇒ ref.path.toString -> pos }
    connection ! UpdatePositions( ps )
  }

  def connectTo( playController: ActorRef ) = {
    val ( enumerator, channel ) = play.api.libs.iteratee.Concurrent.broadcast[ String ]
    this.connection =
      context.actorOf( PlayActorConnection.props( self, channel ), name = "connection" )
    game ! ( playController, inputComponent, Game.Connected( connection, enumerator ) )
  }

  override val receive: Receive = {
    case Connect( playController ) ⇒ connectTo( playController )
    case ClientStarted ⇒
    case Simulation.Snapshot( positions ) ⇒ updateMobilePositions( positions )
    case ClientProxy.PlayerData( mobile, rect ) if mobile != self ⇒ createMobile( mobile, rect )
    case Room.RoomData( fixtures ) ⇒ updateRoomData( fixtures )
    case ClientQuit ⇒ self ! PoisonPill
  }
}