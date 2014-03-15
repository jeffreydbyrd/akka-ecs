package game.communications.commands

import akka.actor.ActorRef

trait PlayerCommand

case object Started extends PlayerCommand
case class Invalid( s: String ) extends PlayerCommand
case class Click( x: Int, y: Int ) extends PlayerCommand

case object Jump extends PlayerCommand
case object GoLeft extends PlayerCommand
case object GoRight extends PlayerCommand
case object Quit extends PlayerCommand

object KeyDown {
  def apply( code: Int ): PlayerCommand = code match {
    case 32 | 38 | 87 ⇒ Jump
    case 65 | 37      ⇒ GoLeft
    case 68 | 39      ⇒ GoRight
    case 81           ⇒ Quit
    case _            ⇒ Invalid( "Unrecognized keycode" )
  }
}

case object StopLeft extends PlayerCommand
case object StopRight extends PlayerCommand

object KeyUp {
  def apply( code: Int ): PlayerCommand = code match {
    case 65 | 37 ⇒ StopLeft
    case 68 | 39 ⇒ StopRight
    case _       ⇒ Invalid( "Unrecognized keycode" )
  }
}