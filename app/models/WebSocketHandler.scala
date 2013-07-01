package models

import akka.actor.Actor
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Concurrent

/**
 * Defines a module used for handling WebSocket connections
 * @author biff
 */
trait WebSocketHandler {

  // Actor Commands:
  case class Message( msg: String )
  case class Start()
  case class Connected( out: Enumerator[ String ] )

  /**
   * An actor that gets instantiated for every new connection.
   * Data from the client is sent to the Player actor, and
   * data from the Player actor is pushed to the 'channel',
   * which connects to the enumerator
   * @author biff
   */
  class WebSocketActor extends Actor {
    val ( enumerator, channel ) = Concurrent.broadcast[ String ]

    override def receive = {
      case Start() ⇒
        sender ! Connected( enumerator )

      case Message( msg ) ⇒
        println( msg )
        channel.push( msg )
    }
  }

}