package game.communications.commands

import scala.Int.int2long

import game.communications.connection.PlayActorConnection
import play.api.libs.json.Json

object PlayerCommand {
  def getCommand( json: String ): PlayerCommand = {
    val parsed = Json.parse( json )
    val data = parsed \ "data"
    ( parsed \ "type" ).as[ String ] match {
      case "started"    ⇒ ClientStarted
      case "ack"        ⇒ PlayActorConnection.Ack( data.as[ Int ] )
      case "GO_LEFT"    ⇒ GoLeft
      case "GO_RIGHT"   ⇒ GoRight
      case "STOP_LEFT"  ⇒ StopLeft
      case "STOP_RIGHT" ⇒ StopRight
      case "JUMP"       ⇒ Jump
      case "QUIT"       ⇒ ClientQuit
      case "click" ⇒
        val x = ( data \ "x" ).as[ Int ]
        val y = ( data \ "y" ).as[ Int ]
        Click( x, y )
      case s ⇒ Invalid( s )
    }
  }
}

trait PlayerCommand

case object ClientStarted extends PlayerCommand
case object ClientQuit extends PlayerCommand
case class Click( x: Int, y: Int ) extends PlayerCommand
case object Jump extends PlayerCommand
case object GoLeft extends PlayerCommand
case object GoRight extends PlayerCommand
case object StopLeft extends PlayerCommand
case object StopRight extends PlayerCommand
case class Invalid( s: String ) extends PlayerCommand
