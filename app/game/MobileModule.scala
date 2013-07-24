package game

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.actor.actorRef2Scala
import play.api.Play.current
import play.api.libs.concurrent.Akka
import akka.actor.Cancellable

trait MobileModule extends EventModule {
  case class Invalid( msg: String ) extends Event
  case class MoveCmd( dist: Int ) extends Event
  case class StopMovingCmd() extends Event

  case class Moved( x: Int, y: Int ) extends Event

  trait EHMobile extends EventHandler with Mobile {
    var moveScheduler: Cancellable = _
    val speed = 1

    def standing: Handle

    def moving: Handle = {
      case e @ MoveCmd( dist ) ⇒
        this.xpos = this.xpos + dist
        this handle Moved( xpos, ypos )
      case StopMovingCmd() ⇒ 
      	moveScheduler.cancel
      	this switchTo standing
      case x               ⇒ this.standard( x )
    }

  }

  trait Mobile {
    val name: String
    var xpos: Int = 0
    var ypos: Int = 0
  }
}