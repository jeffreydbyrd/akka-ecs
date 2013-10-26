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
import game.GameModule
import akka.actor.ActorRef

/**
 * The entire Doppelgamer stack gets composed in this one object.
 */
object Application
    extends Application
    with GameModule {
  override val system: ActorSystem = akka.actor.ActorSystem( "Doppelsystem" )
  override val GAME: ActorRef = system.actorOf( Props( new Game ), name = "game" )
}

/**
 * Defines a controller that serves the client-side engine and handles
 * WebSocket creation.
 * @author biff
 */
trait Application extends Controller with LoggingModule {
  this: GameModule ⇒

  val logger = new PlayLoggingService

  /** Serves the main page */
  def index = Action { Ok { views.html.index() } }

  /**
   * WebSocket.async[String] expects a function Request => (Iteratee[String], Enumerator[String]), where the
   * Iteratee[String] handles incoming messages from the client, and the Enumerator[String] pushes messages
   * to the client. Play will wire everything else together for us.
   *
   * In this case, we ask the game to add a Player with username, and the game sends back an ActorRef that we
   * wire 'in' to. It also sends back a ClientService that has an Enumerator[String] we can give to Play.
   * If something goes wrong, we get a msg:String back, and we return a single-element Enumerator[String]
   * containing the message.
   */
  def websocket( username: String ) = WebSocket.async[ String ] { implicit request ⇒
    ( GAME ? AddPlayer( username ) ) map {
      case ( plr: ActorRef, cs: PlayFrameworkClientService ) ⇒
        val in = Iteratee.foreach[ String ] { json ⇒ plr ! JsonCmd( json ) }
        logger.info( s"Now sending messages directly to ${plr.toString}." )
        ( in, cs.enumerator )

      case msg: String ⇒
        val in = Done[ String, Unit ]( {}, Input.EOF )
        val ret = JsObject( Seq( "error" -> JsString( msg ) ) )
        val out = Enumerator[ String ]( ret.toString ) andThen Enumerator.enumInput( Input.EOF )
        ( in, out )
    }
  }

}