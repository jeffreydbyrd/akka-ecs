package game.entity

import game.components.Component
import game.components.ComponentType
import game.components.io.InputComponent
import game.components.io.ObserverComponent
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