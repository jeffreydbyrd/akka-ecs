package game.mobile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import game.EventModule
import game.world.RoomModule
import game.util.math.LineModule

/**
 * Defines the behavior for all mobile entities (Players and NPCs)
 */
trait MobileModule {
  this: EventModule with LineModule with RoomModule ⇒

  /** How often I move (fixed) */
  val MOVE_INTERVAL = 80

  /** How fast I move (for now, until we get Stats) */
  val SPEED = 1

  /** How high I can jump (for now, until we get Stats) */
  val HOPS = 5

  /** Represents a simple X and Y movement */
  case class Movement( val x: BigDecimal, val y: BigDecimal )

  /**
   * Basic dimensions of a Mobile Position, representing a square in 2D space.
   * The x and y vals are the center of the square.
   */
  case class Position(
      val x: BigDecimal,
      val y: BigDecimal,
      height: Int,
      width: Int ) extends PointLike {
    lazy val head = Point( x, y + ( height / 2 ) )
    lazy val feet = Point( x, y - ( height / 2 ) )
    lazy val right = Point( x + ( width / 2 ), y )
    lazy val left = Point( x - ( width / 2 ), y )
  }

  /** A simple message for telling a Mobile to move */
  case object MoveBitch
  case class Moved( p: Position, m: Movement ) extends Event

  /**
   * An entity with physical dimensions, a position, and that is current moving.
   */
  trait Mobile {
    val name: String
    val height: Int
    val width: Int
    var position: Position
    var movement = Movement( 0, 0 )

    def newPosition( x: BigDecimal, y: BigDecimal ) = Position( x, y, height, width )
  }

  /** A Mobile object that handles Events */
  trait MobileEventHandler extends ActorEventHandler with Mobile {

    /**
     *  An akka scheduler that repeatedly tells this Mobile to move every SPEED millis. It
     *  operates asynchronously, so this Mobile can concurrently react to other messages.
     */
    val moveScheduler = system.scheduler.schedule( 0 millis, MOVE_INTERVAL millis )( self ! MoveBitch )

    // extend the EventHandler's receive method so that it responds to MoveBitch as well
    def moveBitch: Receive = { case MoveBitch ⇒ this emit Moved( position, movement ) }
    override def receive = moveBitch orElse super.receive

    /** The behavior of this Mobile while it's standing still */
    protected def standing: Handle

    /** The behavior of this Mobile while it's moving */
    protected def moving: Handle

    /** Mutates this Mobile's inner position and movement according p and m */
    protected def move( mv: Moved ) {
      this.position = newPosition( mv.p.x + mv.m.x, mv.p.y + mv.m.y )
      this.movement = Movement( movement.x, mv.m.y )
    }

    /** Switches from a standing state to a moving state */
    private def startMoving( xdir: Int ) {
      this.handle = moving orElse default
      movement = Movement( xdir, movement.y )
    }

    /** Switches from a moving state to a standing state */
    protected def stopMoving() = {
      this.handle = standing orElse this.default
      this.movement = Movement( 0, movement.y )
    }

    protected def moveLeft() = startMoving( -SPEED )
    protected def moveRight() = startMoving( SPEED )
    protected def jump() = movement = Movement( movement.x, HOPS )

  }
}