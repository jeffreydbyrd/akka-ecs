package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.actorRef2Scala
import akka.pattern.ask
import engine.communications.commands.ServerCommand
import engine.util.logging.PlayLoggingService
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Iteratee
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import engine.core.Engine
import engine.core.Engine.AddPlayer
import engine.core.Engine.timeout

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
   * In this case, we ask the engine to add a Player with username, and the engine sends back an Enumerator
   * and an ActorRef, which our Iteratee[String] forwards all incoming data to.
   */
  def websocket( username: String ) = WebSocket.async[ String ] { implicit request =>
    logger.info( s"$username requested WebSocket connection" )
    ( Engine.engine ? AddPlayer( username ) ) map {

      case Engine.Connected( connection, enumerator ) => // Success
        val iter = Iteratee.foreach[ String ] { connection ! ServerCommand.getCommand( _ ) }
        ( iter, enumerator )

      case Engine.NotConnected( message ) => // Connection error
        // A finished Iteratee sending EOF
        val iter = Done[ String, Unit ]( (), Input.EOF )
        // Send an error and close the socket
        val enum = Enumerator[ String ]( message ).andThen( Enumerator.enumInput( Input.EOF ) )
        ( iter, enum )
    }
  }
}