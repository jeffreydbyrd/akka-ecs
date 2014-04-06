package game.components.io

import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.communications.commands.ClientQuit
import game.communications.commands.GoLeft
import game.communications.commands.GoRight
import game.communications.commands.Jump
import game.communications.commands.StopJump
import game.communications.commands.StopLeft
import game.communications.commands.StopRight
import game.components.Component
import game.components.Component.RequestSnapshot

object InputComponent {
  val props = Props( classOf[ InputComponent ] )

  // Sent
  case class Snapshot(
    val left: Boolean,
    val right: Boolean,
    val jump: Boolean,
    val quit: Boolean )
}

class InputComponent extends Component {
  import Component._
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