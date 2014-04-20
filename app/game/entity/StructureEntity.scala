package game.entity

import akka.actor.ActorRef
import game.components.ComponentType
import game.components.Component

class StructureEntity( physicalComponent: ActorRef ) extends Entity {
  override val id = EntityId( physicalComponent.path.toString )

  override val components: Map[ ComponentType, ActorRef ] = Map(
    ComponentType.Dimension -> physicalComponent
  )
}