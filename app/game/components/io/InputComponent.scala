package game.components.io

import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import engine.component.Component
import play.api.libs.json.JsValue

object InputComponent {
  val props = Props(classOf[InputComponent])

  // Sent
  case class Snapshot(left: Boolean,
                      right: Boolean,
                      jump: Boolean,
                      quit: Boolean)

}

class InputComponent extends Component {

  import Component._
  import InputComponent._

  var left = false
  var right = false
  var jump = false
  var quit = false

  def exec(cmd: ServerCommand) = cmd match {
    case Jump => jump = true
    case StopJump => jump = false
    case GoLeft => left = true
    case GoRight => right = true
    case StopLeft => left = false
    case StopRight => right = false
    case ClientQuit => quit = true
  }

  override def receive = LoggingReceive {
    case json: JsValue => exec(ServerCommand.getCommand(json))
    case RequestSnapshot =>
      sender ! Snapshot(left, right, jump, quit)
  }
}