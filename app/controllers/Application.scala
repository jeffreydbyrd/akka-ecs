package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import game.PlayerModule
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import play.api.libs.iteratee.Concurrent.Channel

object Application extends Application

/**
 * Defines a controller that serves the client-side engine and handles
 * WebSocket creation.
 * @author biff
 */
trait Application
    extends Controller
    with PlayerModule {

  /**
   * Serves the main page
   */
  def index = Action {
    Ok { views.html.index() }
  }
  
  /**
   * Asynchronously establishes a WebSocket connection using Play's Iteratee-Enumerator model.
   * We instantiate a Player actor and ask for a confirmation that it has started. When it responds with Connected( Enumerator ),
   * we create an Iteratee that forwards incoming messages from the client to the Player. 'In' processes incoming data,
   * while 'out' pushes outgoing data to the client. Populating 'out' is the Player actor's job, and populating 'in' is Play's
   * job. Play is also kind enough to wire 'in' and 'out' to the client for us.
   *
   * If the Player actor responds with NotConnected( msg ), we return 'in' as a 'Done' Iteratee, and 'out' as a single-element
   * Enumerator, delivering 'msg' to the client.
   */
  def websocket( username: String ) = WebSocket.async[ JsValue ] { implicit request ⇒
    val actor = Akka.system.actorOf( Props( new Player( username ) ) )
    ( actor ? Start() ) map {

      case Connected( out ) ⇒
        val in = Iteratee.foreach[ JsValue ] {
          json ⇒ actor ! JsonCmd( json )
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