package game.communications.commands

import game.world.physics.Rect

trait ClientCommand {
  val doCache: Boolean
  def toJson: String
}

case class CreateRect( val id: CmdId,
                       r: Rect,
                       override val doCache: Boolean = false )
    extends ClientCommand {

  override val toJson = s""" {
    "type"      : "create",
    "id"        : "$id",
    "position"  : [${r.x}, ${r.y}],
    "dimensions": [${r.w}, ${r.h}]
  }"""
}

case class CreateLine( val id: CmdId,
                       override val doCache: Boolean = false )
    extends ClientCommand {

  override val toJson = ""
}

case class Move( id: CmdId,
                 x: Float,
                 y: Float,
                 override val doCache: Boolean = false ) extends ClientCommand {

  override val toJson = s"""{
    "type"     : "move",
    "id"       : "$id",
    "position" : [$x, $y]
  }"""
}