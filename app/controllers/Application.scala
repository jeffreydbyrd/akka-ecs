package controllers

import scala.Int.int2long
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.actorRef2Scala
import akka.pattern.ask
import game.Game
import game.Game.AddPlayer
import game.Game.timeout
import game.communications.commands.Click
import game.communications.commands.Invalid
import game.communications.commands.KeyDown
import game.communications.commands.KeyUp
import game.communications.commands.PlayerCommand
import game.communications.commands.Started
import game.util.logging.PlayLoggingService
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import game.communications.connection.PlayActorConnection

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
   * our Iteratee[String] forwards all incoming data to, and an Enumerator.
   */
  def websocket( username: String ) = WebSocket.async[ String ] { implicit request ⇒
    logger.info( s"$username requested WebSocket connection" )
    ( Game.game ? AddPlayer( username ) ) map {

      case Game.Connected( connection, enumerator ) ⇒ // Success
        val iter = Iteratee.foreach[ String ] { connection ! getCommand( _ ) }
        val initMessage = """ {
            "seq":0, "ack":true, "message":{ "type":"started" }
          }	
        	"""
        val enum = Enumerator[ String ]( initMessage ).andThen( enumerator )
        ( iter, enum )

      case Game.NotConnected( message ) ⇒ // Connection error
        // A finished Iteratee sending EOF
        val iter = Done[ String, Unit ]( (), Input.EOF )
        // Send an error and close the socket
        val enum = Enumerator[ String ]( message ).andThen( Enumerator.enumInput( Input.EOF ) )
        ( iter, enum )
    }
  }

  /**
   * Creates a PlayerCommand based on the contents of 'json'. The schema of the content is
   * simply : { type: ..., data: ... }.
   * There are only a few types of commands a client can send: keydown, keyup, click, and ack.
   * Depending on the type, 'data' will be wrapped in the appropriate Event object.
   */
  def getCommand( json: String ): PlayerCommand = {
    val parsed = Json.parse( json )
    val data = parsed \ "data"
    ( parsed \ "type" ).asOpt[ String ].flatMap {
      case "started" ⇒ Some( Started )
      case "ack"     ⇒ data.asOpt[ Int ].map( PlayActorConnection.Ack( _ ) )
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