package game.components.physics

import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.components.Component.RequestSnapshot

object PhysicalComponent {
  def props( x: Float, y: Float, w: Float, h: Float ) =
    Props( classOf[ PhysicalComponent ], x, y, w, h )

  // Received
  case class Update( x: Float, y: Float, w: Float, h: Float )

  // Sent
  case class Snapshot( pos: Position, shape: Rect )
}

class PhysicalComponent( x: Float,
                         y: Float,
                         w: Float,
                         h: Float ) extends Actor {
  import PhysicalComponent._
  import game.components.Component._

  var position = Position( x, y )
  var shape = Rect( w, h )

  override def receive = LoggingReceive {
    case Update( x, y, w, h ) ⇒
      this.position = Position( x, y )
      this.shape = Rect( w, h )

    case RequestSnapshot ⇒ sender ! Snapshot( position, shape )
  }
}