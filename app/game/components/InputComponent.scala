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
import game.communications.commands.ClientQuit
import akka.actor.ActorRef
import akka.event.LoggingReceive

object InputComponent {
  val props = Props( classOf[ InputComponent ] )

  // Received
  case object RequestSnapshot

  // Sent
  case class Snapshot(
    val left: Boolean,
    val right: Boolean,
    val jump: Boolean,
    val quit: Boolean )
}

class InputComponent extends Component {
  import InputComponent._

  var left = false;
  var right = false;
  var jump = false;
  var quit = false;

  override def receive = LoggingReceive {
    case Jump       ⇒ jump = true
    case StopJump   ⇒ jump = false
    case GoLeft     ⇒ left = true
    case GoRight    ⇒ right = true
    case StopLeft   ⇒ left = false
    case StopRight  ⇒ right = false
    case ClientQuit ⇒ quit = true

    case RequestSnapshot ⇒
      sender ! Snapshot( left, right, jump, quit )
  }
}