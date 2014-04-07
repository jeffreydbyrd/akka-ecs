package game.components.physics

import akka.actor.Actor
import akka.actor.Props
import akka.event.LoggingReceive
import game.components.Component.RequestSnapshot

object VelocityComponent {
  def props( x: Float, y: Float ) = Props( classOf[ VelocityComponent ], x, y )

  // received:
  case class Update( x: Float, y: Float )

  // sent:
  case class Snapshot( x: Float, y: Float )
}

class VelocityComponent( var vx: Float, var vy: Float ) extends Actor {
  import VelocityComponent._

  override def receive = LoggingReceive {
    case Update( x, y ) ⇒
      vx = x
      vy = y

    case RequestSnapshot ⇒ sender ! Snapshot( vx, vy )
  }
}