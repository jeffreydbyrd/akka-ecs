package game.communications.commands

import game.components.physics.Rect
import game.components.physics.Position
import play.api.libs.json.Json

/**
 * A Command that goes to the Client (ie. the browser)
 */
trait ClientCommand {
  val typ: String
  val doRetry: Boolean
  def toJson: String
}

object ClientCommand {
  case object ServerReady extends ClientCommand {
    override val typ = "started"
    override val doRetry = true
    override val toJson = null;
  }

  case object ServerQuit extends ClientCommand {
    override val typ = "quit"
    override val doRetry = false
    override val toJson = null;
  }

  case class CreateRect( val id: String,
                         val p: Position,
                         val r: Rect ) extends ClientCommand {
    override val doRetry: Boolean = true
    override val typ = "create";

    override val toJson = s""" {
      "id"        : "$id",
      "position"  : [${p.x}, ${p.y}],
      "dimensions": [${r.w}, ${r.h}]
    }"""
  }

  case class UpdatePositions( positions: Map[ String, ( Float, Float ) ],
                              override val doRetry: Boolean = false ) extends ClientCommand {
    override val typ = "update_positions";

    var json = Json.obj()
    for ( ( id, ( x, y ) ) <- positions ) {
      json += ( id -> Json.arr( x, y ) )
    }

    override val toJson = json.toString
  }

  case class Move( id: String,
                   x: Float,
                   y: Float,
                   override val doRetry: Boolean = false ) extends ClientCommand {
    override val typ = "move"

    override val toJson = s"""{
      "id"       : "$id",
      "position" : [$x, $y]
    }"""
  }
}