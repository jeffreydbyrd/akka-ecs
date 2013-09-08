package game.world

import scala.math._
import game.EventModule
import game.mobile.MobileModule
import game.util.math.LineModule

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
  trait Surface extends Line with AdjustSupplier {
    def inBounds( p: PointLike ): Boolean =
      ( p.x between start.x -> end.x ) && ( p.y between start.y -> end.y )
  }

  trait Floor extends Surface {
    def aboveFloor( p: PointLike ): Boolean = p.y > slope.m * p.x + b
    def belowFloor( p: PointLike ): Boolean = p.y < slope.m * p.x + b
    def onFloor( p: PointLike ): Boolean = p.y == slope.m * p.x + b && inBounds( p )

    val onCollision: Adjust = {
      // Mobile is standing on Floor and attempts to move below it:
      case Moved( ar, p, mv ) if {
        onFloor( p.feet ) && belowFloor( Point( p.feet.x + mv.x, p.feet.y + mv.y ) )
      } ⇒
        val k = hypot( slope.dx, slope.dy ) / mv.x
        val newMv = if ( k isInfinite ) Movement( 0, 0 ) else Movement( slope.dx / k, slope.dy / k )
        Moved( ar, p, newMv )

      // Mobile is above the Floor and wants to move below it:
      case Moved( ar, p, mv ) if {
        aboveFloor( p.feet ) && belowFloor( Point( p.feet.x + mv.x, p.feet.y + mv.y ) )
      } ⇒
        val vector = Vector( start = Point( p.feet.x, p.feet.y ), end = Point( p.feet.x + mv.x, p.feet.y + mv.y ) )
        val inter = Intersection( vector, this )
        val newMovement = if ( inBounds( inter ) ) Movement( inter.x - p.x, inter.y - p.feet.y ) else mv
        Moved( ar, p, newMovement )
    }

    adjusts = List( onCollision )
  }

  case class Wall( xpos: Int,
                   ytop: Int,
                   ybottom: Int ) extends Surface {
    lazy val start = Point( xpos, ytop )
    lazy val end = Point( xpos, ybottom )

    val stop: Adjust = {
      case Moved( ar, p, m ) if inBounds( p.left ) || inBounds( p.right ) ⇒
        Moved( ar, p, Movement( 0, m.y ) )
    }

    //    adjusts = adjusts ::: List( stopLeft, stopRight )
  }

  case class SingleSided( val start: Point, val end: Point ) extends Floor

  case class DoubleSided( val start: Point, val end: Point ) extends Floor
}