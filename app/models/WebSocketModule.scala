package models

import akka.actor.Actor
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue

/**
 * Defines a module used for handling WebSocket connections
 * @author biff
 */
trait WebSocketModule {

  // Actor Commands:
  case class Message( msg: JsValue )
  case class Start()
  case class Connected( out: Enumerator[ JsValue ] )
  case class NotConnected( msg: String )

  /**
   * An actor that gets instantiated for every new connection.
   * Data from the client is sent to the Player actor, and
   * data from the Player actor is pushed to the 'channel',
   * which connects to the enumerator
   * @author biff
   */
  class DgConnectionActor extends DgConnection with Actor {
    val ( enumerator, channel ) = Concurrent.broadcast[ JsValue ]

    override def receive = {
      case Start() ⇒
        sender ! Connected( enumerator )

      case Message( cmd ) ⇒
        println( cmd )
        channel.push( cmd )
    }
  }

  trait DgConnection

}