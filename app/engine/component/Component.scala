package engine.component

import akka.actor.Actor

object Component {
  // Received
  case object RequestSnapshot
}

trait Component extends Actor