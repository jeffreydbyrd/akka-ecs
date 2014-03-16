package game.communications.commands

import game.world.physics.Rect

trait ClientCommand {
  val typ: String
  val doRetry: Boolean
  def toJson: String
}

case object PlayerStarted extends ClientCommand {
  override val typ = "started"
  override val doRetry = true

  override val toJson = """{ "seq":0, "ack":true, "type":"started" }"""
}

case object PlayerQuit extends ClientCommand {
  override val typ = "quit"
  override val doRetry = false
  override val toJson = """{"seq":$seq, "ack":false, "type":"quit"}"""
}

case class CreateRect( val id: CmdId,
                       val r: Rect,
                       override val doRetry: Boolean = false )
    extends ClientCommand {

  override val typ = "create";

  override val toJson = s""" {
    "type"      : "$typ",
    "id"        : "$id",
    "position"  : [${r.x}, ${r.y}],
    "dimensions": [${r.w}, ${r.h}]
  }"""
}

case class CreateLine( val id: CmdId,
                       override val doRetry: Boolean = false )
    extends ClientCommand {

  override val typ = ""

  override val toJson = ""
}

case class Move( id: CmdId,
                 x: Float,
                 y: Float,
                 override val doRetry: Boolean = false ) extends ClientCommand {

  override val typ = "move"

  override val toJson = s"""{
    "type"     : "move",
    "id"       : "$id",
    "position" : [$x, $y]
  }"""
}