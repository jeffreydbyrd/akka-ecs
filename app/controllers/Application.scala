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
   * WebSocket.async[String] expects a function Request => (Iteratee[String], Enumerator[String]), where the
   * Iteratee[String] handles incoming messages from the client, and the Enumerator[String] pushes messages
   * to the client. Play will wire everything else together for us.
   *
   * Here, our Iteratee 'in' forwards messages to the Player Actor. The Player Actor can use 'channel' to
   * send messages to our Enumerator. To account for problems, we ask the Player Actor for confirmation
   * that it started. If it sends back NotConnected( msg ) then instead we return a single-message
   * Enumerator.
   */
  def websocket( username: String ) = WebSocket.async[ String ] { implicit request ⇒
    val ( enumerator, channel ) = Concurrent.broadcast[ String ]
    val cs = new PlayClientService( channel )
    val player = Player.create(username, cs);

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