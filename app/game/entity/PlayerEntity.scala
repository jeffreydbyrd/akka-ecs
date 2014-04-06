package game.entity

import game.components.Component
import game.components.ComponentType
import game.components.io.InputComponent
import game.components.io.ObserverComponent
import akka.actor.ActorRef

class PlayerEntity( inputComponent: ActorRef,
                    observerComponent: ActorRef,
                    physicalComponent:ActorRef) extends Entity {
  override val id: EntityId = EntityId( inputComponent.path.toString )

  override val components: Map[ ComponentType, ActorRef ] = Map(
    ComponentType.Input -> inputComponent,
    ComponentType.Observer -> observerComponent,
    ComponentType.Physical -> physicalComponent
  )
}