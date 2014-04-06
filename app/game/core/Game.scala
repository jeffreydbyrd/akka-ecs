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
  case object Tick
}

sealed class Game extends Actor {
  import Game._

  val stage = context.actorOf( Stage.props, name = "stage" )

  var clients: Map[ String, ActorRef ] = Map()

  val ticker =
    system.scheduler.schedule( 1000 milliseconds, 5000 milliseconds, stage, Tick )

  def createClientProxy( username: String, count: Int ) = {
    val ( enumerator, channel ) = play.api.libs.iteratee.Concurrent.broadcast[ String ]
    val input = context.actorOf( InputComponent.props, s"InputComponent_${count.toString}" )
    val connection =
      context.actorOf( PlayActorConnection.props( input, channel ), s"Conn_${count.toString}" )
    val output =
      context.actorOf( ObserverComponent.props( connection ), s"OutputComponent_${count.toString}" )
    sender ! Connected( connection, enumerator )
    stage ! Stage.Add( new PlayerEntity( input, output ) )
    clients += username -> connection
    context.watch( connection )
  }

  override def receive = managePlayers( 0 )
  def managePlayers( count: Int ): Receive = LoggingReceive {
    case AddPlayer( username ) if !clients.contains( username ) ⇒
      context become managePlayers( count + 1 )
      createClientProxy( username, count )

    case AddPlayer( username ) ⇒
      sender ! NotConnected( s""" {"error" : "username '$username' already in use"} """ )

    case Terminated( connection ) ⇒
      clients = clients.filterNot { case ( usrName, actRef ) ⇒ actRef == connection }
  }

}