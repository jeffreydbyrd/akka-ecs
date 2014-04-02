package game.entity

import akka.actor.ActorRef
import game.components.ComponentType

class Entity(
  val id: EntityId,
  val components: Map[ ComponentType, ActorRef ] = Map() )