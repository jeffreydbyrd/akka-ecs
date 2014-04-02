package game.core

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import game.components.ComponentType
import game.components.InputComponent
import game.entity.EntityId

object Stage {
  def props = Props( classOf[ Stage ] )

  // received messages
}

class Stage extends Actor {
  private var components: Set[ ( ComponentType, ActorRef ) ] = Set()

  override val receive = manageComponents
  val manageComponents: Receive = {
    case component: ( ComponentType, ActorRef ) ⇒ components += component
  }

}