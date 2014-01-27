package game.world.physics

import game.util.math.Slope
import play.api.libs.json.JsObject
import play.api.libs.json.Json

trait Fixture {
  val id: String
  val x: Float
  val y: Float
}

object Fixture {
  def toJson( f: Fixture ): JsObject = {
    val json = Json.obj(
      "type" -> "create",
      "id" -> f.id,
      "position" -> Json.arr( f.x, f.y )
    )

    f match {
      case Rect( _, _, _, w, h )   ⇒ json + ( "dimensions" -> Json.arr( w, h ) )
      case Line( _, _, _, len, m ) ⇒ json + ( "dimensions" -> Json.arr( len, 0 ) )
    }
  }

}

case class Rect( id: String,
                 override val x: Float,
                 override val y: Float,
                 val w: Float,
                 val h: Float ) extends Fixture

case class Line( id: String,
                 override val x: Float,
                 override val y: Float,
                 val len: Float,
                 val m: Slope ) extends Fixture