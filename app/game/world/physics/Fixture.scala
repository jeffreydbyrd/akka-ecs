package game.world.physics

import game.util.math.Slope
import play.api.libs.json.JsObject
import play.api.libs.json.Json

trait Fixture {
  val id: String
  val x: Float
  val y: Float
}

case class Rect( override val id: String,
                 override val x: Float,
                 override val y: Float,
                 val w: Float,
                 val h: Float ) extends Fixture

case class Line( override val id: String,
                 override val x: Float,
                 override val y: Float,
                 val len: Float,
                 val m: Slope ) extends Fixture