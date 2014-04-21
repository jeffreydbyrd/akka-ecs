package game.entities

import engine.component.ComponentType
import akka.actor.ActorRef
import engine.entity.{EntityId, Entity}

class PlayerEntity( inputComponent: ActorRef,
                    observerComponent: ActorRef,
                    dimensionsComponent: ActorRef,
                    mobileComponent: ActorRef ) extends Entity {
  override val id: EntityId = EntityId( inputComponent.path.toString )

  override val components: Map[ ComponentType, ActorRef ] = Map(
    ComponentType.Input -> inputComponent,
    ComponentType.Observer -> observerComponent,
    ComponentType.Dimension -> dimensionsComponent,
    ComponentType.Mobility -> mobileComponent
  )
}