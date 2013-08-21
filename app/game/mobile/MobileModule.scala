package game.mobile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import akka.actor.Cancellable
import game.EventModule
import game.world.RoomModule

/**
 * Defines the behavior for all mobile entities (Players and NPCs)
 */
trait MobileModule extends EventModule {
  this: RoomModule ⇒

  case class Position( x: Int, y: Int ) {
    lazy val top = y + 2
    lazy val bottom = y - 2
    lazy val right = x + 1
    lazy val left = x - 1
  }

  trait Movement {
    val x: Int
    val y: Int
  }
  case class Walking( val x: Int, val y: Int ) extends Movement
  case class Falling( val y: Int ) extends Movement {
    val x = 0
  }

  // Define events:
  case class Invalid( msg: String ) extends Event
  case class KeyUp( code: Int ) extends Event
  case class MoveAttempt( p: Position, m: Movement ) extends Event

  trait Mobile {
    val name: String
    var position: Position
    var xspeed = 1
    var yspeed = 0
  }

  /** An EventHandling Mobile object */
  trait EHMobile extends EventHandler with Mobile {
    private var moveScheduler: Cancellable = _
    private var fallScheduler: Cancellable = system.scheduler.schedule( 0 millis, 80 millis )( this emit MoveAttempt( position, Falling( yspeed ) ) )

    /** a Mobile that is standing still */
    def standing: Handle

    /** Represents the state of a moving Mobile */
    private def moving: Handle = {
      case Moved( ar, p, m ) if ar == self ⇒
        println( position )
        this.position = Position( p.x + m.x, p.y + m.y )
      case KeyUp( 65 | 68 | 37 | 39 ) ⇒
        moveScheduler.cancel
        this.handle = standing ~ this.default
    }

    /** Starts moving this mobile */
    private def move( xdir: Int ) {
      this.handle = moving ~ default
      this.moveScheduler = system.scheduler.schedule( 0 millis, 80 millis )( this emit MoveAttempt( position, Walking( xdir, 0 ) ) )
    }

    protected def moveLeft = move( -xspeed )
    protected def moveRight = move( xspeed )
    protected def jump = this.yspeed = 10

  }
}