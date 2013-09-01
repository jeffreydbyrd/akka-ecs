package game.world

import scala.math._
import game.EventModule
import game.mobile.MobileModule
import game.util.LineModule

/**
 * A surface is an object with length, slope, and position. A surface
 * can be either a Wall or a Floor.
 *
 * A Wall always has a vertical (undefined) slope, while a Surface
 * always has a Defined slope.
 */
trait SurfaceModule extends LineModule with EventModule {
  this: RoomModule with MobileModule ⇒

  /**
   * A Surface is essentially just a line, owned by Room objects, that supplies Adjust
   * functions to modify Mobile Movements
   */
  trait Surface extends Line with AdjustSupplier

  trait Floor extends Surface {
    def redirect( mv: Movement ) = {
      val k = hypot( slope.dx, slope.dy ) / mv.x
      Movement( slope.dx / k, slope.dy / k )
    }

    /**
     *  Stops a Mobile whose Movement will intersect with this Surface.
     *  There are 3 scenarios to check for:
     *    1) no collision --> proceed as normal
     *    2) standing on Surface and moving into slope --> redirect vector
     *    3) in air, colliding with Surface --> cut vector short
     */
    val onCollision: Adjust = {
      case Moved( ar, p, mv ) ⇒
        val v = Vector( Point( p.feet._1, p.feet._2 ), Point( start.x + mv.x, start.y + mv.y ) )
        val inter = Intersection( v )
        val newMovement =
          if ( !inter.isLanding )
            mv
          else if ( v.slope.isDefined && mv.x * slope.m > 0 && ( p.x == inter.x && p.feet._2 == inter.y ) )
            redirect( mv )
          else
            Movement( inter.x - p.x, inter.y - p.feet._2 )
        Moved( ar, p, newMovement )
    }

    adjusts = List( onCollision )
  }

  case class Wall( xpos: Int,
                   ytop: Int,
                   ybottom: Int ) extends Surface {
    lazy val start = Point( xpos, ytop )
    lazy val end = Point( xpos, ybottom )

    def inBounds( p: Position ) = p.head._2 >= ybottom && p.feet._2 <= ytop

    val stopLeft: Adjust = {
      case Moved( ar, p, m ) if p.left._1 == this.xpos && m.x < 0 && inBounds( p ) ⇒
        Moved( ar, p, Movement( 0, m.y ) )
    }

    val stopRight: Adjust = {
      case Moved( ar, p, m ) if p.right._2 == this.xpos && m.x > 0 && inBounds( p ) ⇒
        Moved( ar, p, Movement( 0, m.y ) )
    }

    adjusts = adjusts ::: List( stopLeft, stopRight )
  }

  case class SingleSided( val start: Point, val end: Point ) extends Floor

  case class DoubleSided( val start: Point, val end: Point ) extends Floor
}