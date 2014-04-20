package game.communications.commands

import scala.Int.int2long

import game.communications.connection.PlayActorConnection
import play.api.libs.json.Json

/**
 * A ServerCommand is a command that goes to the Server. Generally directed to a ClientProxy actor
 */
object ServerCommand {
  def getCommand( json: String ): ServerCommand = {
    val parsed = Json.parse( json )
    val data = parsed \ "data"
    ( parsed \ "type" ).as[ String ] match {
      case "started"    => ClientStarted
      case "ack"        => PlayActorConnection.Ack( data.as[ Int ] )
      case "LEFT"       => GoLeft
      case "RIGHT"      => GoRight
      case "JUMP"       => Jump
      case "STOP_LEFT"  => StopLeft
      case "STOP_RIGHT" => StopRight
      case "STOP_JUMP"  => StopJump
      case "QUIT"       => ClientQuit
      case "click" =>
        val x = ( data \ "x" ).as[ Int ]
        val y = ( data \ "y" ).as[ Int ]
        Click( x, y )
      case s => Invalid( s )
    }
  }
}

trait ServerCommand

case object ClientStarted extends ServerCommand
case object ClientQuit extends ServerCommand
case class Click( x: Int, y: Int ) extends ServerCommand
case object Jump extends ServerCommand
case object GoLeft extends ServerCommand
case object GoRight extends ServerCommand
case object StopJump extends ServerCommand
case object StopLeft extends ServerCommand
case object StopRight extends ServerCommand
case class Invalid( s: String ) extends ServerCommand
