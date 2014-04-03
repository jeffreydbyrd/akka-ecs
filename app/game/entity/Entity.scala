package game.entity

import akka.actor.ActorRef
import game.components.ComponentType

trait Entity {
  val id: EntityId
  val components: Map[ ComponentType, ActorRef ]
}