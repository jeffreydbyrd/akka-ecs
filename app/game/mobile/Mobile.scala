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

  var speed: Int
  var hops: Int

  var x: Float
  var y: Float

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
  protected def move( x: Float, y: Float ) {
    this.x = x
    this.y = y
  }

  private def startMoving( xdir: Int ) = {
    context become movingBehavior
    emit( StartedMoving( self, xdir ) )
  }

  protected def stopMoving() = {
    context become standingBehavior
    emit( StoppedMoving( self ) )
  }

  protected def moveLeft() = startMoving( -speed )
  protected def moveRight() = startMoving( speed )
  protected def jump() = {}

}