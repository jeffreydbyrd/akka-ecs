package game.communications.commands

import game.world.physics.Rect
import play.api.libs.json.Json

trait ClientCommand {
  val typ: String
  val doRetry: Boolean
  def toJson: String
}

case object PlayerStarted extends ClientCommand {
  override val typ = "started"
  override val doRetry = true
  override val toJson = null;
}

case object PlayerQuit extends ClientCommand {
  override val typ = "quit"
  override val doRetry = false
  override val toJson = null;
}

case class CreateRect( val id: EntityId,
                       val r: Rect,
                       override val doRetry: Boolean = false )
    extends ClientCommand {

  override val typ = "create";

  override val toJson = s""" {
    "id"        : "$id",
    "position"  : [${r.x}, ${r.y}],
    "dimensions": [${r.w}, ${r.h}]
  }"""
}

case class CreateLine( val id: EntityId,
                       override val doRetry: Boolean = false )
    extends ClientCommand {

  override val typ = ""

  override val toJson = ""
}

case class UpdatePositions( positions: Map[ EntityId, ( Float, Float ) ],
                            override val doRetry: Boolean = false ) extends ClientCommand {
  override val typ = "update_positions";

  var json = Json.obj()
  for ( ( id, ( x, y ) ) ← positions ) {
    json += ( id -> Json.arr( x, y ) )
  }

  override val toJson = json.toString
}

case class Move( id: EntityId,
                 x: Float,
                 y: Float,
                 override val doRetry: Boolean = false ) extends ClientCommand {

  override val typ = "move"

  override val toJson = s"""{
    "id"       : "$id",
    "position" : [$x, $y]
  }"""
}