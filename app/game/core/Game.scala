package game.core

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout.durationToTimeout
import game.communications.proxies.ClientProxy
import game.components.io.InputComponent
import game.entity.EntityId
import game.entity.PlayerEntity
import play.api.libs.iteratee.Enumerator
import akka.actor.Terminated
import game.communications.connection.PlayActorConnection
import scala.concurrent.Future
import game.components.io.ObserverComponent
import game.components.physics.DimensionComponent
import game.components.physics.MobileComponent

object Game {
  // global values:
  implicit val timeout: akka.util.Timeout = 1 second
  val system: ActorSystem = akka.actor.ActorSystem( "Doppelsystem" )
  val game: ActorRef = system.actorOf( Props[ Game ], name = "game" )

  // Received messages
  case class AddPlayer( name: String )

  // Sent messages
  case object Connect
  case class Connected( connection: ActorRef, enum: Enumerator[ String ] )
  case class NotConnected( message: String )
}

sealed class Game extends Actor {
  import Game._

  val engine = context.actorOf( Engine.props, name = "engine" )

  var connections: Map[ String, ActorRef ] = Map()

  def createClientProxy( username: String, count: Int ) = {
    val ( enumerator, channel ) = play.api.libs.iteratee.Concurrent.broadcast[ String ]
    val input = context.actorOf( InputComponent.props, s"input$count" )
    val connection =
      context.actorOf( PlayActorConnection.props( input, channel ), s"conn$count" )
    val output =
      context.actorOf( ObserverComponent.props( connection ), s"observer$count" )
    val dimensions =
      context.actorOf( DimensionComponent.props( 10, 10, 1, 2 ), s"dimensions$count" )
    val velocity =
      context.actorOf( MobileComponent.props( 5, 0 ), s"mobile$count" )
    sender ! Connected( connection, enumerator )
    //engine ! Engine.NewPlayer( new PlayerEntity( input, output, dimensions, velocity ) )
    connections += username -> connection
    context.watch( connection )
  }

  override def receive = managePlayers( 0 )

  def managePlayers( count: Int ): Receive = LoggingReceive {
    case AddPlayer( username ) if !connections.contains( username ) ⇒
      context become managePlayers( count + 1 )
      createClientProxy( username, count )

    case AddPlayer( username ) ⇒
      sender ! NotConnected( s""" {"error" : "username '$username' already in use"} """ )

    case Terminated( connection ) ⇒
      connections = connections.filterNot { case ( usrName, actRef ) ⇒ actRef == connection }
  }

}