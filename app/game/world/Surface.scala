package game.world

import scala.math.BigDecimal
import scala.math.BigDecimal.int2bigDecimal

import game.util.math.Line
import game.util.math.Point
import game.util.math.PointLike

/**
 * A surface is a Line that can be either a Wall or a Floor. A Wall always has a vertical
 * (undefined) slope, while a Floor always has a Defined slope. A Surface is designed to
 * impede a Mobile's Movement. We do this by extending AdjustHandler and providing
 * Adjusts that alter Moved Events. Whatever uses a Surface can use the Adjust functions
 * however it wants. For example, a Room can include them in its 'outgoing' Adjust list:
 *
 * {{{
 * val floor = DoubleSided( Point( 0, 0 ), Point( 200, 0 ) )
 * outgoing = outgoing ::: floor.outgoing
 * }}}
 *
 * In fact, the RoomEventHandler does just that to make sure Mobiles don't fall into
 * infinity.
 */
object Surface {
  implicit val rm = BigDecimal.RoundingMode.HALF_UP
}

/**
 * A Surface is essentially just a line, owned by Room objects, that supplies Adjust
 * functions to modify Moved Events
 */
trait Surface extends Line {
  /**
   * Returns true if 'p' is within the X and Y bounds of this Surface. If you draw
   * a box around this Surface, with the Start & End points resting in the corners,
   * then 'p' must be within that box. If the Surface is flat, then 'p' must be
   * resting directly on the Surface.
   */
  def inBounds( p: PointLike ): Boolean =
    ( start.x <= p.x && p.x <= end.x ) && ( start.y <= p.y && p.y <= end.y )
}

trait Floor extends Surface

case class Wall( xpos: Int,
                 ytop: Int,
                 ybottom: Int ) extends Surface {
  lazy val start = Point( xpos, ytop )
  lazy val end = Point( xpos, ybottom )
}

case class SingleSided( val start: Point, val end: Point ) extends Floor

case class DoubleSided( val start: Point, val end: Point ) extends Floor