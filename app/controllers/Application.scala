package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import game.util.logging.PlayLoggingService
import game.communications.PlayActorConnection._
import game.communications.RetryingConnection.Ack
import game.mobile.Player._
import game.events.Event
import game.Game
import game.mobile.Player

/**
 * Defines a Play controller that serves the client-side engine and handles
 * WebSocket creation.
 */
object Application extends Controller {
  import Game._

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
      Player.StartResponse( conn ) ← ( game ? AddPlayer( username ) )
      ReturnEnum( out ) ← conn ? GetEnum
      in = Iteratee.foreach[ String ] { conn ! getCommand( _ ) }
    } yield ( in, out )
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
      case "ack"     ⇒ data.asOpt[ Int ].map( Ack( _ ) )
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