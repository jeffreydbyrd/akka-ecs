package game.components

import akka.actor.Actor
import game.entity.EntityId

object Component {
  // Received
  case object RequestSnapshot
}

trait Component extends Actor