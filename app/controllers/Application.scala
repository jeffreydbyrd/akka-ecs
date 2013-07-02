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
import models.WebSocketModule
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Input
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

object Application extends Application

/**
 * Defines a controller that serves the client-side engine and handles
 * WebSocket creation.
 * @author biff
 */
trait Application extends Controller with WebSocketModule {

  /**
   * Serves the main page
   */
  def index = Action {
    Ok { views.html.index() }
  }

  implicit val timeout = akka.util.Timeout( 1 second )

  /**
   * Asynchronously establishes a WebSocket connection using Play's Iteratee-Enumerator model.
   * Not only is the function asynchronous, it uses an Akka actor to maintain state.
   *
   * We instantiate a WebSocketActor and ask for a confirmation that it has started. When it responds with Connected( Enumerator ),
   * we create an Iteratee that forwards incoming messages from the client to the WebSocketActor. 'in' processes incoming data,
   * while 'out' pushes outgoing data to the client. Populating 'out' is the WebSocketActor's job, and populating 'in' is Play's
   * job. Play is also kind enough to wire 'in' and 'out' to the client for us.
   *
   * If the WebSocketActor responds with NotConnected( msg ), we return 'in' as a 'Done' Iteratee, and 'out' as a single-element
   * Enumerator, delivering 'msg' to the client.
   */
  def websocket = WebSocket.async[ JsValue ] { implicit request ⇒
    val actor = Akka.system.actorOf( Props( new DgConnectionActor ) )
    ( actor ? Start() ) map {

      case Connected( out ) ⇒
        val in = Iteratee.foreach[ JsValue ] { event ⇒
          actor ! Message( event )
        }
        ( in, out )

      case NotConnected( msg ) ⇒
        val in = Done[ JsValue, Unit ]( {}, Input.EOF )
        val ret = JsObject( Seq( "error" -> JsString( msg ) ) )
        val out = Enumerator[ JsValue ]( ret ) andThen Enumerator.enumInput( Input.EOF )
        ( in, out )
    }
  }

}