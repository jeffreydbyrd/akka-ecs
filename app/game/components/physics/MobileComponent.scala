package game.components.physics

import akka.actor.Actor
import akka.actor.Props
import akka.event.LoggingReceive
import game.components.Component.RequestSnapshot

object MobileComponent {
  def props( x: Float, y: Float ) = Props( classOf[ MobileComponent ], x, y )

  // received:
  case class Update( x: Float, y: Float )

  // sent:
  case class Snapshot( speed: Float, hops: Float )
}

/**
 * speed: think "meters / sec"
 * hops: a number between 0.0 and 1.0 (percent gravity)
 */
class MobileComponent( var speed: Float, var hops: Float ) extends Actor {
  import MobileComponent._

  override def receive = LoggingReceive {
    case Update( x, y ) =>
      speed = x
      hops = y

    case RequestSnapshot => sender ! Snapshot( speed, hops )
  }
}