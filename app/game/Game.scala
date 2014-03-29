package game

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Cancellable
import akka.actor.Props
import akka.event.LoggingReceive
import akka.util.Timeout.durationToTimeout
import game.world.Room
import scala.concurrent.duration._
import scala.concurrent.Future
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Enumerator
import game.mobile.ClientProxy
import game.communications.connection.PlayActorConnection
import akka.actor.Actor
import play.api.libs.iteratee.Concurrent.Channel

object Game {
  // global values:
  implicit val timeout: akka.util.Timeout = 1 second
  val system: ActorSystem = akka.actor.ActorSystem( "Doppelsystem" )
  val game: ActorRef = system.actorOf( Props[ Game ], name = "game" )

  // Received messages
  case class AddPlayer( name: String )

  // Sent messages
  case class Connected( connection: ActorRef, enum: Enumerator[ String ] )
  case class NotConnected( message: String )
  case object Tick
}

sealed class Game extends Actor {
  import Game._

  // We all share one room for now
  val room = context.actorOf( Room.props( "TEMP" ), name = "temp" )

  var players: Map[ String, ActorRef ] = Map()

  val ticker =
    system.scheduler.schedule( 100 milliseconds, 20 milliseconds, self, Tick )

  override def receive = LoggingReceive {
    case Tick ⇒ room ! Tick

    case AddPlayer( username ) if !players.contains( username ) ⇒
      val proxy = context.actorOf(
        ClientProxy.props( username, sender, room ),
        name = username
      )
      players += username -> proxy

    case AddPlayer( username ) ⇒
      sender ! NotConnected( s""" {"error" : "username '$username' already in use"} """ )

    case ClientProxy.Quit( ref ) ⇒ players = players filterNot { case ( s, ar ) ⇒ ar == ref }
  }

}