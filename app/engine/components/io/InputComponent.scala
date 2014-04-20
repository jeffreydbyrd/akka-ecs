package engine.components.io

import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import engine.communications.commands.ClientQuit
import engine.communications.commands.GoLeft
import engine.communications.commands.GoRight
import engine.communications.commands.Jump
import engine.communications.commands.StopJump
import engine.communications.commands.StopLeft
import engine.communications.commands.StopRight
import engine.components.Component
import engine.components.Component.RequestSnapshot

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
    case Jump       => jump = true
    case StopJump   => jump = false
    case GoLeft     => left = true
    case GoRight    => right = true
    case StopLeft   => left = false
    case StopRight  => right = false
    case ClientQuit => quit = true

    case RequestSnapshot =>
      sender ! Snapshot( left, right, jump, quit )
  }
}