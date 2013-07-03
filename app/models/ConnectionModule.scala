package models

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.pattern.ask
import models.Commands.Connected
import models.Commands.Command
import models.Commands.Start
import models.Commands.Update
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsValue
import models.Commands.NotConnected
import akka.actor.PoisonPill

/**
 * Defines a module used for handling WebSocket connections
 * @author biff
 */
trait ConnectionModule {
  this: PlayerModule ⇒

  implicit val timeout = akka.util.Timeout( 1 second )

  /**
   * An actor that gets instantiated for every new connection.
   * Data from the client is sent to the Player actor, and
   * data from the Player actor is pushed to the 'channel',
   * which connects to the enumerator
   * @author biff
   */
  class ConnectionActor extends Connection with Actor {
    override def receive = {
      // this is basically a constructor for the actor
      case Start( username: String ) ⇒
        val ( enumerator, channel ) = Concurrent.broadcast[ JsValue ]
        this.channel = channel
        this.player = Player( username )
        establishConnection(self, sender, enumerator)
        context become router
    }

    def router: Receive = {
      case cmd: Command     ⇒ player forward cmd
      case Update( json ) ⇒ channel.push( json )
    }

  }

  /**
   * A Connection has a Channel that it pushes data to. A Channel connects to
   * an Enumerator, but this trait doesn't care which. A Channel can connect
   * to multiple Enumerators and "broadcast" data to them.
   */
  trait Connection {
    var channel: Channel[ JsValue ] = _
    var player: ActorRef = _

    /**
     * Tells the player to Start and, giving it the 'connection'. If player responds with
     * no status, then everything is assumed to have gone smootly, and enum is sent to the
     * sender. If the player does return a status, then we send the error message to the
     * sender and kill the connection and player.
     */
    def establishConnection( connection: ActorRef, sender: ActorRef, enum: Enumerator[ JsValue ] ) {
      player ? Start( connection ) foreach {
        case None ⇒ sender ! Connected( enum )
        case Some( msg: String ) ⇒
          sender ! NotConnected( msg )
          List( connection, player ) map ( _ ! PoisonPill ) // failed to connect... you know what to do :(
      }
    }
  }

}