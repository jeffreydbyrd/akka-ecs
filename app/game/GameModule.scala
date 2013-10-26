package game

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import game.mobile.PlayerModule
import game.util.logging.LoggingModule
import game.world.RoomModule
import akka.actor.ActorRef

/*
 * Defines the top-level actor exposed to the world.
 */
trait GameModule
    extends EventModule
    with PlayerModule
    with RoomModule {

  implicit val TIMEOUT = akka.util.Timeout( 1.second )

  val GAME: ActorRef

  // ====
  // Game Commands
  // ====
  case class AddPlayer( name: String )

  class Game extends ActorEventHandler {
    // TODO: build the world from database

    /** We all share one room for now */
    lazy val ROOMREF = context.actorOf( Props( new Room( "temp" ) ), name = "temp" )

    def listening: Receive = {
      case ap @ AddPlayer( username ) ⇒
        logger.info( s"received AddPlayer( $username )." )
        /*
         * TODO:
         *   - get the Player data from database service.
         *   - Get the actor path of the player from the data
         *   - Get the ActorRef of the player's room
         */
        ROOMREF forward ap
    }

    override def receive = listening orElse super.receive

    override def default = {
      case _ ⇒
    }
  }
}