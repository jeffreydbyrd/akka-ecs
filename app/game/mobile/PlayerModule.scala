package game.mobile

import scala.concurrent.duration.DurationInt
import scala.math.BigDecimal.int2bigDecimal
import scala.util.parsing.json.JSON
import scala.util.parsing.json.JSONObject

import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import game.ConnectionModule
import game.world.RoomModule

/** Defines a module used for handling a Player */
trait PlayerModule extends MobileModule with ConnectionModule {
  this: RoomModule ⇒

  /** We all share one room for now */
  lazy val ROOMREF = Room.create( "temp" )

  implicit val TIMEOUT = akka.util.Timeout( 1.second )

  //======================================
  // Player-Client Communication
  //======================================
  /** Used to tell a Player Actor to start. The caller should wait for a confirmation */
  case object Start
  /** Used to tell the App that it's connected */
  case object Connected
  /** Used to tell the App that it's not connected */
  case class NotConnected( msg: String )
  /** Used by the App to deliver a raw JSON formatted string to the Player */
  case class JsonCmd( msg: String )

  // Events:
  /** Used to signal that an incoming command from the client was invalid */
  case class Invalid( msg: String ) extends Event
  case class KeyUp( code: Int ) extends Event
  case class KeyDown( code: Int ) extends Event
  case class Click( x: Int, y: Int ) extends Event
  case object Quit extends Event

  trait GenericPlayer[ D ] extends Mobile {
    val cs: ClientService[ D ]
    val height = 4
    val width = 2
    /**
     * Sets up this Player object by retrieving state from the database.
     * If something goes wrong, we return Some( errMsg ),
     * otherwise we return None to indicate that everything's fine.
     */
    protected def setup: Option[ String ] = None
  }

  /**
   * An asynchronous EventHandlerActor that handles communication
   * with the client and also interacts with the game world.
   */
  trait PlayerEventHandler
      extends MobileEventHandler
      with GenericPlayer[ String ] {

    val cs: ClientService[ String ]

    // Override the EventHandler's receive function because we don't want to handle Events yet
    override def receive = { case Start ⇒ start }
    def playing: Receive = { case JsonCmd( json ) ⇒ handle( getCommand( json ) ) }

    /**
     *  Constructs the Player Actor by fetching data from the database. entering the World,
     *  and reporting to the App on its success (Connected or NotConnected). If the Player
     *  fails to start, he must kill himself. If he succeeds, he
     */
    def start =
      setup map { msg ⇒ // if there's a message then something went wrong
        logger.error( msg )
        sender ! NotConnected( msg )
        self ! PoisonPill // failed to start... you know what to do :(
      } getOrElse {
        sender ! Connected
        this.handle = standing orElse default

        // Switch to normal EventHandler behavior, with our extra Playing behavior to handle JsonCmds
        context become { playing orElse super.receive }
      }

    override def default: Handle = {
      case Click( x: Int, y: Int ) ⇒
      case Invalid( msg: String )  ⇒ logger.warn( msg )
      case KeyUp( 81 )             ⇒ self ! PoisonPill
      case KeyDown( 32 | 38 | 87 ) ⇒ jump()
      case evt @ Moved( p, m ) if sender == self ⇒
        move( evt )
        cs send s""" { "x" : ${position.x}, "y" : ${position.y} } """
      case _ ⇒
    }

    override def standing: Handle = {
      case KeyDown( 65 | 37 ) ⇒ moveLeft()
      case KeyDown( 68 | 39 ) ⇒ moveRight()
    }

    override def moving: Handle = {
      case KeyUp( 65 | 68 | 37 | 39 ) ⇒ stopMoving()
    }

    override def postStop {
      this emit Quit
      this.subscribers foreach { _ ! Unsubscribe }
      cs send "quit"
      this.moveScheduler.cancel
      cs.close
    }

  }

  class Player( val name: String, override val cs: ClientService[ String ] ) extends PlayerEventHandler {
    //temporary:
    var position = newPosition( 10, 30 )
    override def setup = {
      logger.info( "%s joined the game".format( this.name ) )
      subscribers = subscribers :+ ROOMREF
      ROOMREF ! Subscribe
      None
    }
  }

  object Player {
    def create( username: String, cs: ClientService[ String ] ) =
      system.actorOf( Props( new Player( username, cs ) ), name = username )
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