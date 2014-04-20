package engine.entity

import engine.components.Component
import engine.components.ComponentType
import engine.components.io.InputComponent
import engine.components.io.ObserverComponent
import akka.actor.ActorRef

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