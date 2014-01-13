package game.mobile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.math.BigDecimal.int2bigDecimal
import akka.actor.actorRef2Scala
import game.Game
import game.events.Event
import game.events.EventHandler
import game.events.Handle
import game.util.math.Point
import game.util.math.PointLike
import akka.actor.ActorRef
import akka.event.LoggingReceive

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

object Mobile {
  /** How often I move (fixed) */
  val MOVE_INTERVAL = 80

  /** How fast I move (for now, until we get Stats) */
  val SPEED = 5

  /** How high I can jump (for now, until we get Stats) */
  val HOPS = 5

  case class Moved( mobile: ActorRef, p: Position, m: Movement ) extends Event

  /** A simple message for telling a Mobile to move */
  trait Command
  case object EmitMovement extends Command
}

/**
 * An `EventHandler` that emits a `Moved` `Event` on every `Tick` that it receives. However
 * it has no opinion on when it should and shouldn't move. It provides basic methods for
 * making the transitions happen (moveLeft, moveRight, move, stopMoving). Classes
 * implementing this trait must define the Receive behavor for `standing`, `moving`, and
 * `mobileBehavior`.
 */
trait Mobile extends EventHandler {
  import Mobile._

  val name: String
  val height: Int
  val width: Int

  var position: Position
  var movement = Movement( 0, 0 )

  /** Defines the messages this Mobile responds to while standing still */
  protected def standing: Receive

  /** Defines the messages this Mobile responds to while moving */
  protected def moving: Receive

  /** Core logic relevant to whatever class is implementing Mobile */
  protected def mobileBehavior: Receive

  /**
   *  An akka scheduler that repeatedly tells this Mobile to move every SPEED millis. It
   *  operates asynchronously, so this Mobile can concurrently react to other messages.
   */
  protected val moveScheduler = Game.system.scheduler.schedule( 0 millis, MOVE_INTERVAL millis )( self ! EmitMovement )

  val emitMovement: Receive = {
    case EmitMovement ⇒ emit( Moved( self, position, movement ) )
  }

  private def standingBehavior = LoggingReceive { standing orElse mobileBehavior orElse emitMovement }
  private def movingBehavior = LoggingReceive { moving orElse mobileBehavior orElse emitMovement }

  override def receive: Receive = standingBehavior

  /** Mutates this Mobile's inner position and movement according to p and m */
  protected def move( mv: Moved ) {
    this.position = newPosition( mv.p.x + mv.m.x, mv.p.y + mv.m.y )
    this.movement = Movement( movement.x, mv.m.y )
  }

  private def startMoving( xdir: Int ) = {
    context become movingBehavior
    this.movement = Movement( xdir, movement.y )
  }

  /** Puts this Mobile back into a `standing` state */
  protected def stopMoving() = {
    context become standingBehavior
    this.movement = Movement( 0, movement.y )
  }

  def newPosition( x: BigDecimal, y: BigDecimal ) = Position( x, y, height, width )
  protected def moveLeft() = startMoving( -SPEED )
  protected def moveRight() = startMoving( SPEED )
  protected def jump() = movement = Movement( movement.x, HOPS )

}