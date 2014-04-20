package engine.entity

import akka.actor.ActorRef
import engine.components.ComponentType
import engine.components.Component

class StructureEntity( physicalComponent: ActorRef ) extends Entity {
  override val id = EntityId( physicalComponent.path.toString )

  override val components: Map[ ComponentType, ActorRef ] = Map(
    ComponentType.Dimension -> physicalComponent
  )
}