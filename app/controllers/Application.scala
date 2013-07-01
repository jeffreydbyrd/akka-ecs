package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import views._
import play.api.mvc.WebSocket
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.PushEnumerator
import akka.actor.Actor
import akka.actor.Props
import play.api.libs.iteratee.Concurrent
import akka.pattern._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import models.WebSocketHandler

object Application extends Application

/**
 * Defines a controller that serves the client-side engine and handles
 * WebSocket creation.
 * @author biff
 */
trait Application extends Controller with WebSocketHandler {

  /**
   * Serves the main page
   */
  def index = Action {
    Ok { views.html.index() }
  }

  implicit val timeout = akka.util.Timeout( 1 second )

  /**
   * Establishes a WebSocket connection using Play's Iteratee-Enumerator model.
   * In order to maintain state, we use the WebSocketActor and ask for a
   * confirmation that it has started. The Connected response contains
   * an Enumerator that will spit out data to the client.
   * The Iteratee consumes data coming from the client. Play wires these
   * constructs together behind the scenes. All we need to know is that
   * the Iteratee simply forwards the data on to the WebSocketActor
   */
  def websocket = WebSocket.async[ String ] { implicit request ⇒
    val actor = Akka.system.actorOf( Props( new WebSocketActor ) )
    ( actor ? Start() ) map {
      case Connected( out ) ⇒
        val in = Iteratee.foreach[ String ] { event ⇒
          actor ! Message( event )
        }
        ( in, out )
    }
  }

}