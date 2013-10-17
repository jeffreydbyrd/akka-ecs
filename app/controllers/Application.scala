package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import game.ConnectionModule
import game.mobile.PlayerModule
import game.world.RoomModule
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import game.world.SurfaceModule
import game.util.logging.LoggingModule

/**
 * The entire Doppelgamer stack gets composed in this one object.
 */
object Application
    extends Application
    with PlayerModule
    with ConnectionModule
    with LoggingModule
    with RoomModule
    with SurfaceModule {
  override val system: ActorSystem = akka.actor.ActorSystem( "Doppelsystem" )
}

/**
 * Defines a controller that serves the client-side engine and handles
 * WebSocket creation.
 * @author biff
 */
trait Application extends Controller with LoggingModule {
  this: PlayerModule ⇒

  /** Serves the main page */
  def index = Action { Ok { views.html.index() } }

  /**
   * Asynchronously establishes a WebSocket connection using Play's Iteratee-Enumerator model.
   * We instantiate a Player actor and ask for a confirmation that it has started. When it responds with Connected,
   * we create an Iteratee that forwards incoming messages from the client to the Player. 'In' processes incoming data,
   * while 'enumerator' pushes outgoing data to the client. 
   *
   * Play! populates 'in' for us while the 'channel' populates 'enumerator'. 
   * Player will send messages to the channel (using the ClientService).
   *
   * If the Player actor responds with NotConnected( msg ), we return 'in' as a 'Done' Iteratee, and 'out' as a single-element
   * Enumerator, delivering 'msg' to the client.
   */
  def websocket( username: String ) = WebSocket.async[ String ] { implicit request ⇒
    val ( enumerator, channel ) = Concurrent.broadcast[ String ]
    val cs = new PlayClientService( channel )
    val player = system.actorOf( Props( new Player( username, cs ) ) )

    ( player ? Start ) map {

      case Connected ⇒
        val in = Iteratee.foreach[ String ] { json ⇒ player ! JsonCmd( json ) }
        ( in, enumerator )

      case NotConnected( msg ) ⇒
        val in = Done[ String, Unit ]( {}, Input.EOF )
        val ret = JsObject( Seq( "error" -> JsString( msg ) ) )
        val out = Enumerator[ String ]( ret.toString ) andThen Enumerator.enumInput( Input.EOF )
        cs.close
        ( in, out )
    }
  }

}