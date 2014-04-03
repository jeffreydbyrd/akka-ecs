package game.communications.proxies

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import game.communications.commands.ClientQuit
import game.communications.commands.ClientStarted
import game.communications.commands.CreateRect
import game.communications.commands.UpdatePositions
import game.communications.connection.PlayActorConnection
import game.core.Game
import game.core.Game.Connect
import game.world.Room
import game.world.physics.Fixture
import game.world.physics.Rect
import game.world.physics.Simulation
import game.communications.commands.ServerCommand
import akka.event.LoggingReceive

object ClientProxy {
  def props( inputComp: ActorRef ) = Props( classOf[ ClientProxy ], inputComp )
}

/**
 * Represents a Client (remote or local... doesn't matter). The ClientProxy doesn't hold any
 * state about its character. It merely provides commands and consumes updates that it delivers
 * to the real Client. The ClientProxy attaches to a Room
 */
class ClientProxy( val inputComponent: ActorRef ) extends Actor {
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

  def connect() = {
    val ( enumerator, channel ) = play.api.libs.iteratee.Concurrent.broadcast[ String ]
    this.connection =
      context.actorOf( PlayActorConnection.props( inputComponent, channel ), name = "connection" )
    sender ! Game.Connected( connection, enumerator )
  }

  override val receive: Receive = LoggingReceive {
    case Connect                          ⇒ connect()
    case Simulation.Snapshot( positions ) ⇒ updateMobilePositions( positions )
  }

}