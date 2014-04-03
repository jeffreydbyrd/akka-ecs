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
import game.components.InputComponent
import game.entity.EntityId
import game.entity.PlayerEntity
import play.api.libs.iteratee.Enumerator
import akka.actor.Terminated

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
    system.scheduler.schedule( 100 milliseconds, 2000 milliseconds, stage, Tick )

  def createClientProxy( username: String, count: Int ) = {
    context become managePlayers( count + 1 )
    val inputComp = context.actorOf( InputComponent.props, s"InputComponent_${count.toString}" )
    val proxy = context.actorOf( ClientProxy.props( inputComp ), username )
    context.watch( proxy )
    clients += username -> proxy
    val playController = sender // outer ref to sender b/c it will change
    ( proxy ? Connect ) map {
      case conn: Connected ⇒
        playController ! conn
        stage ! Stage.Add( new PlayerEntity( EntityId( count ), inputComp, proxy ) )
    }
  }

  override def receive = managePlayers( 0 )
  def managePlayers( count: Int ): Receive = LoggingReceive {
    case AddPlayer( username ) if !clients.contains( username ) ⇒
      createClientProxy( username, count )

    case AddPlayer( username ) ⇒
      sender ! NotConnected( s""" {"error" : "username '$username' already in use"} """ )

    case Terminated( ref ) ⇒
      clients = clients filterNot { case ( s, ar ) ⇒ ar == ref }
  }

}