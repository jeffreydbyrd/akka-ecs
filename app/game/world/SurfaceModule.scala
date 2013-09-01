package game.world

import scala.math._

import game.EventModule
import game.mobile.MobileModule

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
    /** Stops a Mobile whose Movement will intersect with this Surface */
    val stop: Adjust = {
      case evt @ Moved( ar, p, mv ) ⇒
        val ( x0, y0 ) = p.feet
        val x1 = x0 + mv.x
        val y1 = y0 + mv.y // x,y of end position
        val _m = ( y1 - y0 ) / ( x1 - x0 ) // slope of mobile's trajectory
        val _b = y0 - ( _m * x0 ) // mobile's y-axis intercept
        val xinter = ( b - _b ) / ( _m - slope.m ) // mobile-surface x-intercept
        val yinter = _m * xinter + b // mobile-surface y-intercept

        val startToEnd = hypot( x1 - x0, y1 - y0 ) // distance from start to end position
        val startToIntercept = hypot( x0 - xinter, y0 - yinter ) // distance from start to intercept

        if ( ( startToEnd >= startToIntercept ) &&
          ( xinter between start.x -> end.x ) &&
          ( yinter between start.y -> end.y ) )
          Moved( ar, Position( xinter, yinter ), Movement( mv.x, 0 ) )
        else
          evt
    }

    adjusts = List( stop )
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