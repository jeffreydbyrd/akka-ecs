package game

import scala.concurrent.duration.DurationInt

import akka.actor.PoisonPill
import akka.actor.actorRef2Scala
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsPath
import play.api.libs.json.JsValue
import play.api.libs.json.__

/**
 * Defines a module used for handling a Player
 * @author biff
 */
trait PlayerModule extends MobileModule {
  this: RoomModule ⇒

  implicit val timeout = akka.util.Timeout( 1 second )

  // Player-Client Communication
  case class Start()
  case class Connected( out: Enumerator[ JsValue ] )
  case class NotConnected( msg: String )
  case class JsonCmd( msg: JsValue )

  // Events:
  case class KeyDown( code: Int ) extends Event
  case class Click( x: Int, y: Int ) extends Event

  class Player( val name: String ) extends EHPlayer {
    //temporary:
//    val roomRef = system.actorOf( Props( classOf[ Room ], "temp" ) )
//    subscribers = subscribers :+ roomRef
//    this emit Arrived()
  }

  /**
   * An asynchronous EventHandlerActor that handles communication
   * with the client and also interacts with the game world.
   */
  trait EHPlayer
      extends EHMobile
      with GenericPlayer {

    abstract override def receive = {
      case Start()         ⇒ start
      case JsonCmd( json ) ⇒ handle( getCommand( json ) )
      case x               ⇒ super[ EHMobile ].receive( x )
    }

    // this is basically a constructor for the actor
    def start =
      setup map { msg ⇒ // if there's a message then something went wrong
        sender ! NotConnected( msg )
        self ! PoisonPill // failed to start... you know what to do :(
      } getOrElse {
        val ( enumerator, channel ) = Concurrent.broadcast[ JsValue ]
        sender ! Connected( enumerator )
        this.channel = channel
        this.handle = standing ~ default

      }

    override def default: Handle = {
      case Click( x: Int, y: Int ) ⇒
      case Invalid( msg: String )  ⇒
      case _                       ⇒
    }

    def standing: Handle = {
      case KeyDown( 65 ) ⇒ standing( KeyDown( 37 ) )
      case KeyDown( 68 ) ⇒ standing( KeyDown( 39 ) )
      case KeyDown( 37 ) ⇒ moveLeft
      case KeyDown( 39 ) ⇒ moveRight
    }

  }

  /**
   * A Player has a Channel that it pushes data to. A Channel connects to
   * an Enumerator, but this trait doesn't care which. A Channel can connect
   * to multiple Enumerators and "broadcast" data to them.
   */
  trait GenericPlayer extends Mobile {
    var channel: Channel[ JsValue ] = _

    /**
     * Sets up this Player object by retrieving state from the database.
     * If something goes wrong, we return Some[String] to deliver an error message,
     * otherwise we return None to indicate that everything's fine.
     */
    protected def setup: Option[ String ] = {
      None
    }
  }

  /**
   * Creates a Command object based on the contents of 'json'. The schema of the content is
   * simply : { type: ..., data: ...}.
   * There are only a few types of commands a client can send: keydown, keyup, click.
   * Depending on the type, 'data' will be wrapped in the appropriate Command object.
   * If there is an error while parsing, Invalid[ String ] is returned.
   */
  def getCommand( json: JsValue ): Event = {
    // defines a function that composes a string of error messages for every path in 'errs'
    // and returns an Invalid command object
    val errFun =
      ( errs: Seq[ ( JsPath, Seq[ ValidationError ] ) ] ) ⇒
        Invalid( ( for { ( p, seq ) ← errs; e1 ← seq } yield { e1.message } ).mkString( ", " ) )

    ( json \ "type" ).validate[ String ].fold( errFun, str ⇒ {
      val datapath = ( __ \ "data" )
      val key_ = ( datapath.read[ Int ] ).reads( json )
      str match {
        case "keyup"   ⇒ key_.fold( errFun, KeyUp )
        case "keydown" ⇒ key_.fold( errFun, KeyDown )
        case "click" ⇒
          ( ( datapath \ "x" ).read[ Int ] ~ ( datapath \ "y" ).read[ Int ] )( Click ).reads( json ).fold( errFun, x ⇒ x )
      }
    } )
  }

}