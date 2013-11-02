package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import game.communications.ConnectionModule
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
import scala.concurrent.Future
import scala.util.parsing.json.JSON
import scala.util.parsing.json.JSONObject
import play.api.libs.json.Json

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
   * In this case, we ask the game to add a Player with username, and the game sends back an ActorRef, which
   * our Iteratee[String] forwards all incoming data to. Since we can expect this ActorRef to represent a
   * `PlayActorConnection`, we can also ask for its Enumerator[String] `out`.
   */
  def websocket( username: String ) = WebSocket.async[ String ] { implicit request ⇒
    for {
      conn ← ( GAME ? AddPlayer( username ) ).mapTo[ ActorRef ]
      ReturnEnum( out ) ← conn ? GetEnum
    } yield {
      val in = Iteratee.foreach[ String ] { json ⇒ conn ! getCommand( json ) }
      logger.info( s"Now sending messages directly to ${conn.toString()}." )
      ( in, out )
    }
  }

  /**
   * Creates an Event based on the contents of 'json'. The schema of the content is
   * simply : { type: ..., data: ... }.
   * There are only a few types of commands a client can send: keydown, keyup, click, and ack.
   * Depending on the type, 'data' will be wrapped in the appropriate Event object.
   */
  def getCommand( json: String ): Event = {
    val parsed = Json.parse( json )
    val data = parsed \ "data"
    ( parsed \ "type" ).asOpt[ String ].flatMap {
      case "ack"     ⇒ data.asOpt[ Long ].map( Ack( _ ) )
      case "keyup"   ⇒ data.asOpt[ Int ].map( KeyUp( _ ) )
      case "keydown" ⇒ data.asOpt[ Int ].map( KeyDown( _ ) )
      case "click" ⇒ for {
        x ← ( data \ "x" ).asOpt[ Int ]
        y ← ( data \ "y" ).asOpt[ Int ]
      } yield Click( x, y )
    } getOrElse {
      Invalid
    }
  }
}