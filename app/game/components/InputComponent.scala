package game.components

import game.entity.EntityId
import akka.actor.Props

object InputComponent {
  def props( id: EntityId ) = Props( classOf[ InputComponent ], id )
  case object Input extends ComponentType
}

class InputComponent( val id: EntityId ) extends Component {
  override def receive = {

    case _ ⇒
  }
}