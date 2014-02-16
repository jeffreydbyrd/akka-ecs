package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.actorRef2Scala
import akka.pattern.ask
import game.Game.AddPlayer
import game.Game.timeout
import game.communications.PlayActorConnection.GetEnum
import game.communications.PlayActorConnection.ReturnEnum
import game.events.Event
import game.mobile.Player
import game.mobile.Player.Click
import game.mobile.Player.Invalid
import game.mobile.Player.KeyDown
import game.mobile.Player.KeyUp
import game.util.logging.PlayLoggingService
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import game.communications.RetryingActorConnection
import game.Game
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Enumerator

/**
 * Defines a Play controller that serves the client-side engine and handles
 * WebSocket creation.
 */
object Application extends Controller {
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
    ( Game.game ? AddPlayer( username ) ).map {

      case Game.Connected( connection, enumerator ) ⇒
        val iter = Iteratee.foreach[ String ] { connection ! getCommand( _ ) }
        ( iter, enumerator )

      case Game.NotConnected( message ) ⇒ // Connection error
        // A finished Iteratee sending EOF
        val iter = Done[ String, Unit ]( (), Input.EOF )
        // Send an error and close the socket
        val enum = Enumerator[ String ]( message ).andThen( Enumerator.enumInput( Input.EOF ) )
        ( iter, enum )
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
      case "ack"     ⇒ data.asOpt[ Int ].map( RetryingActorConnection.Ack( _ ) )
      case "keyup"   ⇒ data.asOpt[ Int ].map( KeyUp( _ ) )
      case "keydown" ⇒ data.asOpt[ Int ].map( KeyDown( _ ) )
      case "click" ⇒ for {
        x ← ( data \ "x" ).asOpt[ Int ]
        y ← ( data \ "y" ).asOpt[ Int ]
      } yield Click( x, y )
    } getOrElse {
      Invalid( json )
    }
  }
}