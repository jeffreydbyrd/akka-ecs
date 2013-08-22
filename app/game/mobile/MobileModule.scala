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

  case class Movement( val x: Int, val y: Int )

  // Define events:
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
    private var fallScheduler: Cancellable = system.scheduler.schedule( 80 millis, 80 millis )( this emit MoveAttempt( position, Movement( 0, yspeed ) ) )

    protected def standing: Handle
    protected def moving: Handle

    protected def move(p:Position) {
      this.position = p
    }
    
    protected def move( x: Int, y: Int ) {
      position = Position( position.x + x, position.y + y )
      yspeed = y
    }

    private def startMoving( xdir: Int ) {
      this.handle = moving ~ default
      this.moveScheduler = system.scheduler.schedule( 0 millis, 80 millis )( this emit MoveAttempt( position, Movement( xdir, 0 ) ) )
    }

    protected def stopMoving {
      moveScheduler.cancel
      this.handle = standing ~ this.default
    }

    protected def moveLeft = startMoving( -xspeed )
    protected def moveRight = startMoving( xspeed )
    protected def jump = this.yspeed = 10

  }
}