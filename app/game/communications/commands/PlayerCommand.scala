package game.communications.commands

import akka.actor.ActorRef

trait PlayerCommand

case object Started extends PlayerCommand
case class Invalid( s: String ) extends PlayerCommand
case class KeyUp( code: Int ) extends PlayerCommand
case class KeyDown( code: Int ) extends PlayerCommand
case class Click( x: Int, y: Int ) extends PlayerCommand