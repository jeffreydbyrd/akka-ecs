package game.core

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import game.components.ComponentType
import game.components.InputComponent
import game.entity.EntityId
import game.systems.EchoSystem
import game.entity.Entity

object Stage {
  def props = Props( classOf[ Stage ] )

  // received messages
}

class Stage extends Actor {
  private var entities: Set[ Entity ] = Set()
  private val echoSystem: ActorRef = context.actorOf( EchoSystem.props )

  override val receive = manageComponents
  val manageComponents: Receive = {
    case ent: Entity ⇒ entities += ent
  }

}