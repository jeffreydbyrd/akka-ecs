package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.pattern.ask
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Iteratee
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import game.systems.connection.ConnectionSystem
import ConnectionSystem.AddPlayer
import akka.util.Timeout
import game.systems.connection.ConnectionSystem

/**
 * Defines a Play controller that serves the client-side engine and handles
 * WebSocket creation.
 */
object PlayController extends Controller {
  import play.Logger

  implicit val timeout = Timeout(1.second)

  /** Serves the main page */
  def index = Action {
    Ok {
      views.html.index()
    }
  }

  /**
   * WebSocket.async[String] expects a function Request => (Iteratee[String], Enumerator[String]), where the
   * Iteratee[String] handles incoming messages from the client, and the Enumerator[String] pushes messages
   * to the client. Play will wire everything else together for us.
   *
   * In this case, we ask the engine to add a Player with username, and the engine sends back an Enumerator
   * and an ActorRef, which our Iteratee[String] forwards all incoming data to.
   */
  def websocket(username: String) = WebSocket.async[String] {
    implicit request =>
      Logger.info(s"$username requested WebSocket connection")
      (game.MyGame.connectionSystem ? AddPlayer(username)) map {

        case ConnectionSystem.Connected(connection, enumerator) => // Success
          val iter = Iteratee.foreach[String] {
            connection ! _
          }
          (iter, enumerator)

        case ConnectionSystem.NotConnected(message) => // Connection error
          val iter = Done[String, Unit]((), Input.EOF)
          val enum = Enumerator[String](message).andThen(Enumerator.enumInput(Input.EOF))
          (iter, enum)
      }
  }
}