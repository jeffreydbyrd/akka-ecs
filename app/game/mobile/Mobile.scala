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

object Mobile {
  // How fast I move (for now, until we get Stats)
  val SPEED = 5

  // How high I can jump (for now, until we get Stats)
  val HOPS = 5

  // Received Events:
  case class Moved( mobile: ActorRef, x: Float, y: Float ) extends Event

  // Sent Events
  case class StartedMoving( mobile: ActorRef, x: Int ) extends Event
  case class StoppedMoving( mobile: ActorRef ) extends Event
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

  var x: Float = 50
  var y: Float = 100

  /** Defines the messages this Mobile responds to while standing still */
  protected def standing: Receive

  /** Defines the messages this Mobile responds to while moving */
  protected def moving: Receive

  /** Core logic relevant to whatever class is implementing Mobile */
  protected def mobileBehavior: Receive

  private def standingBehavior = LoggingReceive { standing orElse mobileBehavior orElse eventHandler }
  private def movingBehavior = LoggingReceive { moving orElse mobileBehavior orElse eventHandler }

  override def receive: Receive = standingBehavior

  /** Mutates this Mobile's inner position and movement according to p and m */
  protected def move( mv: Moved ) {
    this.x = mv.x
    this.y = mv.y
  }

  private def startMoving( xdir: Int ) = {
    context become movingBehavior
    emit( StartedMoving( self, xdir ) )
  }

  protected def stopMoving() = {
    context become standingBehavior
    emit( StoppedMoving( self ) )
  }

  protected def moveLeft() = startMoving( -SPEED )
  protected def moveRight() = startMoving( SPEED )
  protected def jump() = {}

}