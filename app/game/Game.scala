package game

import scala.concurrent.duration.DurationInt
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.util.Timeout.durationToTimeout
import game.events.EventHandler
import game.world.Room
import akka.event.LoggingReceive

object Game {
  implicit val timeout: akka.util.Timeout = 1 second
  val system: ActorSystem = akka.actor.ActorSystem( "Doppelsystem" )

  val game: ActorRef = system.actorOf( Props( new Game ), name = "game" )

  case class AddPlayer( name: String )
}

sealed class Game extends EventHandler {
  // TODO: build the world from database

  /** We all share one room for now */
  lazy val ROOMREF = context.actorOf( Props( new Room( "temp" ) ), name = "temp" )

  def listening: Receive = {
    case ap @ Game.AddPlayer( username ) ⇒
      /*
         * TODO:
         *   - get the Player data from database service.
         *   - Get the actor path of the player from the data
         *   - Get the ActorRef of the player's room
         */
      ROOMREF forward ap
  }

  override def receive = { listening }
}