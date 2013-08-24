package game.mobile

import scala.concurrent.duration.DurationInt
import scala.util.parsing.json.JSON
import scala.util.parsing.json.JSONObject

import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import game.ConnectionModule
import game.world.RoomModule

/**
 * Defines a module used for handling a Player
 * @author biff
 */
trait PlayerModule extends MobileModule {
  this: RoomModule with ConnectionModule ⇒

  implicit val timeout = akka.util.Timeout( 1 second )

  // Player-Client Communication
  case object Start
  case object Connected
  case class NotConnected( msg: String )
  case class JsonCmd( msg: String )

  // Events:
  case class Invalid( msg: String ) extends Event
  case class KeyUp( code: Int ) extends Event
  case class KeyDown( code: Int ) extends Event
  case class Click( x: Int, y: Int ) extends Event
  case object Quit extends Event

  trait GenericPlayer[ D ] extends Mobile {
    val cs: ClientService[ D ]

    /**
     * Sets up this Player object by retrieving state from the database.
     * If something goes wrong, we return Some( errMsg ),
     * otherwise we return None to indicate that everything's fine.
     */
    protected def setup: Option[ String ] = None // temporary placeholder
  }

  /**
   * An asynchronous EventHandlerActor that handles communication
   * with the client and also interacts with the game world.
   */
  trait EHPlayer
      extends EHMobile
      with GenericPlayer[ String ] {

    override def receive = { case Start ⇒ start }
    def playing: Receive = { case JsonCmd( json ) ⇒ handle( getCommand( json ) ) }

    // this is basically a constructor for the actor
    def start =
      setup map { msg ⇒ // if there's a message then something went wrong
        sender ! NotConnected( msg )
        self ! PoisonPill // failed to start... you know what to do :(
      } getOrElse {
        sender ! Connected
        this.handle = standing orElse default
        context become { playing orElse super.receive }
      }

    def printPosition {
      cs send s"(${position.x}, ${position.y})"
      println( s"$position , $movement" )
    }

    override def default: Handle = {
      case Click( x: Int, y: Int ) ⇒
      case Invalid( msg: String )  ⇒
      case KeyUp( 81 )             ⇒ self ! PoisonPill
      case KeyDown( 32 | 38 | 87 ) ⇒ jump
      case Moved( `self`, p, m )   ⇒ move( p, m )
      case _                       ⇒
    }

    override def standing: Handle = {
      case KeyDown( 65 | 37 ) ⇒ moveLeft()
      case KeyDown( 68 | 39 ) ⇒ moveRight()
    }

    override def moving: Handle = {
      case KeyUp( 65 | 68 | 37 | 39 ) ⇒ stopMoving()
    }

    override def move( p: Position, m: Movement ) {
      super.move( p, m )
      printPosition
    }

    override def postStop {
      this emit Quit
      this.subscribers foreach { _ ! Unsubscribe }
      cs send "quit"
      this.moveScheduler.cancel
      cs.close
    }

  }

  class Player( val name: String, val cs: ClientService[ String ] ) extends EHPlayer {
    //temporary:
    var position = Position( 10, 30 )
    override def setup = {
      println( "player setup.." );
      val roomRef = system.actorOf( Props( new Room( "temp" ) ) )
      subscribers = subscribers :+ roomRef
      roomRef ! Subscribe
      None
    }
  }

  /**
   * Creates a Command object based on the contents of 'json'. The schema of the content is
   * simply : { type: ..., data: ... }.
   * There are only a few types of commands a client can send: keydown, keyup, click.
   * Depending on the type, 'data' will be wrapped in the appropriate Event object.
   * If there is an error while parsing, Invalid is returned.
   */
  def getCommand( json: String ): Event = JSON.parseRaw( json ) match {
    case Some( JSONObject( map ) ) ⇒ ( map.get( "type" ), map.get( "data" ) ) match {
      case ( Some( "keyup" ), Some( d: Double ) )   ⇒ KeyUp( d.toInt )
      case ( Some( "keydown" ), Some( d: Double ) ) ⇒ KeyDown( d.toInt )
      case ( Some( "click" ), Some( JSONObject( pos: Map[ String, Any ] ) ) ) ⇒
        ( pos.get( "x" ), pos.get( "y" ) ) match {
          case ( Some( x: Double ), Some( y: Double ) ) ⇒ Click( x.toInt, y.toInt )
          case _ ⇒
            Invalid( "A click command expects 'x' and 'y' integer values" )
        }
      case _ ⇒ Invalid( "Unrecognized command." )
    }
    case _ ⇒ Invalid( "Failed to parse JSON string." )
  }

}