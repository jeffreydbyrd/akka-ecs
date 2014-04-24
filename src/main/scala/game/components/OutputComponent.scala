package game.components

import doppelengine.component.Component
import akka.actor.Props

object OutputComponent {
  val props = Props(classOf[OutputComponent])

  // received:
  case class Msg(s: String)

}

class OutputComponent extends Component {

  import OutputComponent._

  override def receive: Receive = {
    case Msg(s) => println(s)
  }
}
