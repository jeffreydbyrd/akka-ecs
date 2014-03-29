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

object ClientProxy {
  def props( name: String,
             client: ActorRef,
             room: ActorRef ) =
    Props( classOf[ ClientProxy ], name, client, room )

  // Sent Messages
  trait Command
  trait MoveAttempt extends Command
  case object GoLeft extends MoveAttempt
  case object GoRight extends MoveAttempt
  case class WalkAttempt( mobile: ActorRef, x: Int ) extends MoveAttempt
  case class JumpAttempt( mobile: ActorRef ) extends MoveAttempt
  case class Quit( mob: ActorRef )
  case class PlayerData( mobile: ActorRef, dims: Rect )
}

/**
 * Represents a Client (remote or local... doesn't matter). The ClientProxy doesn't hold any
 * state about its character. It merely provides commands and consumes updates that it delivers
 * to the real Client. The ClientProxy attaches to a Room
 */
class ClientProxy( val name: String,
                   val _client: ActorRef,
                   var room: ActorRef ) extends Actor {
  import ClientProxy._

  var dimensions = Rect( name, 5, 25, 1, 2 )
  var connection: ActorRef = _

  def updateRoomData( fixtures: Iterable[ game.world.physics.Fixture ] ) =
    for ( f ← fixtures ) f match {
      case r: Rect ⇒ connection ! CreateRect( r.id, r, true )
    }

  def createMobile( mobile: ActorRef, rect: Rect ) =
    connection ! CreateRect( mobile.path.toString, rect, true )

  def updateMobilePositions( positions: Map[ ActorRef, ( Float, Float ) ] ) = {
    val ps = positions.map { case ( ref, pos ) ⇒ ref.path.toString -> pos }
    connection ! UpdatePositions( ps )
  }

  override val receive: Receive = {
    case ClientStarted ⇒ room ! Room.Arrived( self, dimensions )
    case ClientProxy.PlayerData( mobile, rect ) if mobile != self ⇒ createMobile( mobile, rect )
    case Room.RoomData( fixtures ) ⇒ updateRoomData( fixtures )
    case Simulation.Snapshot( positions ) ⇒ updateMobilePositions( positions )
    case Jump ⇒ room ! JumpAttempt( self )
    case ClientQuit ⇒ self ! PoisonPill
  }

  override def postStop = room ! Quit( self )

  override def preStart = {
    val ( enumerator, channel ) = play.api.libs.iteratee.Concurrent.broadcast[ String ]
    this.connection =
      context.actorOf( PlayActorConnection.props( self, channel ), name = "connection" )
    _client ! Game.Connected( connection, enumerator )
  }
}