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
 * An entity with physical dimensions, a position, and that is current moving.
 */
trait Mobile extends EventHandler {
  import Mobile._

  val name: String
  val height: Int
  val width: Int

  var position: Position
  var movement = Movement( 0, 0 )

  /** The behavior of this Mobile while it's standing still */
  protected def standing: Receive

  /** The behavior of this Mobile while it's moving */
  protected def moving: Receive

  /** The logic that ultimately calls `move`, `startMoving`, and `stopMoving` */
  protected def mobileBehavior: Receive

  /**
   *  An akka scheduler that repeatedly tells this Mobile to move every SPEED millis. It
   *  operates asynchronously, so this Mobile can concurrently react to other messages.
   */
  val moveScheduler = Game.system.scheduler.schedule( 0 millis, MOVE_INTERVAL millis )( self ! EmitMovement )

  override def receive: Receive = {
    case EmitMovement ⇒ emit( Moved( self, position, movement ) )
  }

  /** Mutates this Mobile's inner position and movement according p and m */
  protected def move( mv: Moved ) {
    this.position = newPosition( mv.p.x + mv.m.x, mv.p.y + mv.m.y )
    this.movement = Movement( movement.x, mv.m.y )
  }

  protected def startMoving( xdir: Int ) = {
    context become { standing orElse mobileBehavior orElse receive }
    this.movement = Movement( 0, movement.y )
  }

  protected def stopMoving() = {
    context become { standing orElse mobileBehavior orElse receive }
    this.movement = Movement( 0, movement.y )
  }

  def newPosition( x: BigDecimal, y: BigDecimal ) = Position( x, y, height, width )
  protected def moveLeft() = startMoving( -SPEED )
  protected def moveRight() = startMoving( SPEED )
  protected def jump() = movement = Movement( movement.x, HOPS )

}