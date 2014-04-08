package game.components.physics

import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.components.Component.RequestSnapshot

object DimensionComponent {
  def props( x: Float, y: Float, w: Float, h: Float ) =
    Props( classOf[ DimensionComponent ], x, y, w, h )

  // Received
  case class UpdateAll( x: Float, y: Float, w: Float, h: Float )
  case class UpdatePosition( x: Float, y: Float )

  // Sent
  case class Snapshot( pos: Position, shape: Rect )
}

class DimensionComponent( x: Float, y: Float,
                          w: Float, h: Float ) extends Actor {
  import DimensionComponent._
  import game.components.Component._

  var position = Position( x, y )
  var shape = Rect( w, h )

  override def receive = LoggingReceive {
    case UpdateAll( x, y, w, h ) ⇒
      position = Position( x, y )
      shape = Rect( w, h )

    case UpdatePosition( x, y ) ⇒ position = Position( x, y )

    case RequestSnapshot        ⇒ sender ! Snapshot( position, shape )
  }
}