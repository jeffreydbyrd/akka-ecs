package game

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Cancellable
import akka.actor.Props
import akka.event.LoggingReceive
import akka.util.Timeout.durationToTimeout
import game.events.Event
import game.events.EventHandler
import game.world.Room
import scala.concurrent.duration._
import scala.concurrent.Future
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Enumerator

object Game {
  // global values:
  implicit val timeout: akka.util.Timeout = 1 second
  val system: ActorSystem = akka.actor.ActorSystem( "Doppelsystem" )
  val game: ActorRef = system.actorOf( Props[ Game ], name = "game" )

  // Received messages
  case class AddPlayer( name: String )

  // Sent messages
  case class NewPlayer( client: ActorRef, name: String ) extends Event
  case object Tick extends Event
}

sealed class Game extends EventHandler {
  import Game._

  // We all share one room for now
  subscribers += context.actorOf( Room.props( "TEMP" ), name = "temp" )

  val ticker =
    system.scheduler.schedule( 100 milliseconds, 20 milliseconds, self, Tick )

  override def receive = LoggingReceive {
    case Tick                  ⇒ emit( Tick )
    case AddPlayer( username ) ⇒ emit( NewPlayer( sender, username ) )
  }

}