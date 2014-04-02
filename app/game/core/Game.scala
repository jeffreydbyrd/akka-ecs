package game.core

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.LoggingReceive
import akka.util.Timeout.durationToTimeout
import scala.concurrent.duration._
import play.api.libs.iteratee.Enumerator
import akka.actor.Actor
import game.core.Stage
import game.components.InputComponent
import game.entity.EntityId
import game.components.ComponentType
import akka.actor.actorRef2Scala
import game.communications.proxies.ClientProxy
import game.entity.Entity

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

  override def receive = managePlayers( 0 )
  def managePlayers( count: Int ): Receive = LoggingReceive {
    case Tick ⇒ stage ! Tick

    case AddPlayer( username ) if !clients.contains( username ) ⇒
      val inputComp = context.actorOf(
        InputComponent.props( EntityId( count ) ),
        name = s"InputComponent_${count.toString}"
      )
      val proxy = context.actorOf( ClientProxy.props( self, inputComp ), name = username )
      clients += username -> proxy
      proxy ! Connect( sender )
      context become managePlayers( count + 1 )

    case AddPlayer( username ) ⇒
      sender ! NotConnected( s""" {"error" : "username '$username' already in use"} """ )

    case ( play: ActorRef, inputComp: ActorRef, conn: Connected ) ⇒
      play ! conn
      stage ! new Entity(
        EntityId( count ),
        Map( ComponentType.Client -> sender, ComponentType.Input -> inputComp )
      )

    case ClientProxy.Quit( ref ) ⇒ clients = clients filterNot { case ( s, ar ) ⇒ ar == ref }
  }

}