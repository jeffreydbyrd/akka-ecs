package game

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.actor.actorRef2Scala
import play.api.Play.current
import play.api.libs.concurrent.Akka
import akka.actor.Cancellable
import akka.actor.Props

/**
 * Defines the behavior for all mobile entities (Players and NPCs)
 */
trait MobileModule extends EventModule {
  this: RoomModule ⇒

  // Define events:
  case class Invalid( msg: String ) extends Event
  case class KeyUp( code: Int ) extends Event
  case class MoveAttempt( dir: Int ) extends Event

  /** An EventHandling Mobile object */
  trait EHMobile extends EventHandler with Mobile {
    private var moveScheduler: Cancellable = _
    val speed = 1

    //temporary:
    val roomRef = system.actorOf( Props( new Room( "temp" ) ) )
    subscribers = subscribers :+ roomRef
    this emit Arrived()

    /** a Mobile that is standing still */
    def standing: Handle

    /** Represents the state of a moving Mobile */
    def moving: Handle = {
      case Moved( ar, dist ) if ar == self ⇒
        this.xpos = this.xpos + dist
        println( this.xpos );
      case KeyUp( code: Int ) if List( 65, 68, 37, 39 ) contains code ⇒
        moveScheduler.cancel
        this.handle = standing ~ this.default
    }

    /** Starts moving this mobile */
    def move( dir: Int ) {
      this.handle = moving ~ default
      this.moveScheduler = Akka.system.scheduler.schedule( 0 millis, 80 millis )( this emit MoveAttempt( dir ) )
    }

    def moveLeft = move( -speed )
    def moveRight = move( speed )

  }

  trait Mobile {
    val name: String
    var xpos: Int = 0
    var ypos: Int = 0
  }
}