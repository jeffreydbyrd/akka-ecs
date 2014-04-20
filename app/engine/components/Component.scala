package engine.components

import akka.actor.Actor
import engine.entity.EntityId

object Component {
  // Received
  case object RequestSnapshot
}

trait Component extends Actor