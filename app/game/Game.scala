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
import game.core.Stage
import game.components.InputComponent
import game.entity.EntityId

object Game {
  // global values:
  implicit val timeout: akka.util.Timeout = 1 second
  val system: ActorSystem = akka.actor.ActorSystem( "Doppelsystem" )
  val game: ActorRef = system.actorOf( Props[ Game ], name = "game" )

  // Received messages
  case class AddPlayer( name: String )

  // Sent messages
  case class Connect( playController: ActorRef )
  case class Connected( connection: ActorRef, enum: Enumerator[ String ] )
  case class NotConnected( message: String )
  case object Tick
}

sealed class Game extends Actor {
  import Game._

  // We all share one room for now
  val stage = context.actorOf( Stage.props, name = "STAGE" )

  var clients: Map[ String, ActorRef ] = Map()

  val ticker =
    system.scheduler.schedule( 100 milliseconds, 20 milliseconds, self, Tick )

  override def receive = manageEntities( 0 )
  def manageEntities( count: Int ): Receive = LoggingReceive {
    case Tick ⇒ stage ! Tick

    case AddPlayer( username ) if !clients.contains( username ) ⇒
      val inputComp = context.actorOf(
        InputComponent.props( EntityId( count ) ),
        name = s"InputComponent_${count.toString}"
      )
      val proxy = context.actorOf( ClientProxy.props( self, inputComp ), name = username )
      clients += username -> proxy
      proxy ! Connect( sender )
      context become manageEntities( count + 1 )

    case AddPlayer( username ) ⇒
      sender ! NotConnected( s""" {"error" : "username '$username' already in use"} """ )

    case ( play: ActorRef, inputComp: ActorRef, conn: Connected ) ⇒
      play ! conn
      stage ! ( ???, sender )
      stage ! ( InputComponent.Input, inputComp )

    case ClientProxy.Quit( ref ) ⇒ clients = clients filterNot { case ( s, ar ) ⇒ ar == ref }
  }

}