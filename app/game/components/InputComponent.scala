package game.components

import game.entity.EntityId
import akka.actor.Props
import scala.collection.immutable.Queue
import game.communications.commands.ServerCommand
import game.communications.commands.Jump
import game.communications.commands.StopJump
import game.communications.commands.GoLeft
import game.communications.commands.StopLeft
import game.communications.commands.StopRight
import game.communications.commands.GoRight
import akka.actor.ActorRef

object InputComponent {
  def props( id: EntityId ) = Props( classOf[ InputComponent ], id )

  case object RequestSnapshot
}

class InputComponent extends Component {
  import InputComponent._

  var left = false;
  var right = false;
  var jump = false;

  override def receive = {
    case RequestSnapshot ⇒ ( left, right, jump )

    case Jump            ⇒ jump = true
    case StopJump        ⇒ jump = false
    case GoLeft          ⇒ left = true
    case GoRight         ⇒ right = true
    case StopLeft        ⇒ left = false
    case StopRight       ⇒ right = false
  }
}