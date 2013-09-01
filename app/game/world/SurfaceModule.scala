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
trait SurfaceModule {
  this: RoomModule with EventModule with MobileModule ⇒

  // just for fun:
  // Implicit conversions that basically give Ints/Doubles a 'between' function
  case class IntBetween( i: Int ) { def between( ns: ( Int, Int ) ) = ( ns._1 <= i && i <= ns._2 ) || ( ns._1 >= i && i >= ns._2 ) }
  implicit def intBetween( i: Int ) = IntBetween( i )
  case class DoubleBetween( d: Double ) { def between( ns: ( Double, Double ) ) = ( ns._1 <= d && d <= ns._2 ) || ( ns._1 >= d && d >= ns._2 ) }
  implicit def doublBetween( d: Double ) = DoubleBetween( d )

  class UndefinedSlopeException extends Exception

  trait Slope {
    def dx: Double
    def dy: Double
    lazy val m = dy / dx
  }

  abstract class Defined( val dx: Double, val dy: Double ) extends Slope

  case class Slant( private val _x: Double, private val _y: Double ) extends Defined( _x, _y )

  case object Flat extends Defined( 1, 0 )

  case object Undefined extends Slope {
    val dx: Double = 0
    def dy = throw new UndefinedSlopeException
  }

  /**
   * A convenience object for quickly creating Slopes
   */
  object Slope {
    def apply( dx: Double, dy: Double ) = ( dx, dy ) match {
      case ( 0, _ ) ⇒ Undefined
      case ( _, 0 ) ⇒ Flat
      case _        ⇒ Slant( dx, dy )
    }
  }

  case class Point( x: Double, y: Double )

  /**
   * A Surface is essentially just a line with a length, position (x,y), and a slope.
   * Surfaces are owned by Room objects, and can supply Adjusts to modify certain Events.
   */
  trait Surface extends AdjustSupplier {
    val start: Point
    val end: Point
    val length: Double
    val slope: Slope
  }

  trait Floor extends Surface {
    lazy val length = hypot( start.x - end.x, start.y - end.y )
    lazy val slope = Slope( start.x - end.x, start.y - end.y )
    lazy val b = start.y - ( slope.m * start.x )

    def isLanding( position: ( Double, Double ), mv: Movement ) = {
      val ( x0, y0 ) = position // x,y of start position
      val x1 = x0 + mv.x
      val y1 = y0 + mv.y // x,y of end position
      val _m = ( y1 - y0 ) / ( x1 - x0 ) // slope of mobile's trajectory
      val _b = y0 - ( _m * x0 ) // mobile's y-axis intercept
      val xinter = ( b - _b ) / ( _m - slope.m ) // mobile-surface x-intercept
      val yinter = _m * xinter + b // mobile-surface y-intercept

      val startToEnd = hypot( x1 - x0, y1 - y0 ) // distance from start to end position
      val startToIntercept = hypot( x0 - xinter, y0 - yinter ) // distance from start to intercept

      ( startToEnd >= startToIntercept ) &&
        ( xinter between start.x -> end.x ) &&
        ( yinter between start.y -> end.y )
    }

    /** Stops a Mobile whose Movement will intersect with this Surface */
    val stop: Adjust = {
      case evt @ Moved( ar, p, mv ) if isLanding( p.feet, mv ) ⇒
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
          ???
        else
          evt
    }

    adjusts = List( stop )
  }

  case class Wall( xpos: Int,
                   ytop: Int,
                   ybottom: Int ) extends Surface {
    val slope = Undefined
    lazy val start = Point( xpos, ytop )
    lazy val end = Point( xpos, ybottom )
    lazy val length = ytop - ybottom.toDouble

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