package game.systems

import doppelengine.entity.Entity
import doppelengine.system.System
import doppelengine.system.System.UpdateEntities
import game.components.OutputComponent
import akka.actor.Props
import game.components.types.Output

object InputSystem {
  val props = Props(classOf[InputSystem])
}

class InputSystem extends System {

  var entities:Set[Entity] = Set()

  override def receive: Receive = {
    case UpdateEntities(_, ents) =>
      entities = ents

    case s:String => for (e <- entities) e(Output) ! OutputComponent.Msg(s)
  }
}
