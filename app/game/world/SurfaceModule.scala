package game.world

import game.EventModule
import game.mobile.MobileModule
import scala.math._

/**
 * A surface is an object with length, slope, and position. A surface
 * can be either a Wall or a Floor.
 *
 * A Wall always has a vertical (undefined) slope, while a Surface
 * always has a Defined slope.
 */
trait SurfaceModule {
  this: RoomModule with EventModule with MobileModule ⇒

  class UndefinedSlopeException extends Exception

  trait Slope {
    def dx: Int
    def dy: Int
    lazy val m = dy.toDouble / dx.toDouble
  }

  abstract class Defined( val dx: Int, val dy: Int ) extends Slope

  case class Slant( private val _x: Int, private val _y: Int ) extends Defined( _x, _y )

  case object Flat extends Defined( 1, 0 )

  case object Undefined extends Slope {
    val dx = 0
    def dy = throw new UndefinedSlopeException
  }

  /**
   * A Surface is essentially just a line with a length, position (x,y), and a slope.
   * Surfaces are owned by Room objects, and can supply Adjusts to modify certain Events.
   */
  trait Surface extends AdjustSupplier {
    val xpos: Int
    val ypos: Int
    val length: Int
    val slope: Slope
  }

  trait Floor extends Surface {
    val slope: Defined
    lazy val b = ypos - ( slope.m * xpos )

    def isLanding( position: ( Double, Double ), mv: Movement ) = {
      val ( x0, y0 ) = position
      val _m = ( ( mv.y + y0 ) - y0 ) / ( ( mv.x + x0 ) - x0 )
      val _b = y0 - ( _m * x0 )
      val xinter = ( b - _b ) / ( _m - slope.m )
      
      /*
       * TODO: finish the above logic:
       * figure out x,y-intercept and if I am crossing that point
       */

      val yintersect = slope.m * ( x0 + mv.x ) + b
      mv.y <= 0 &&
        y0 >= yintersect &&
        ( y0 + mv.y ) <= yintersect
    }

    def inBounds( p: Position ) = {
      val c = sqrt( pow( slope.dx, 2 ) + pow( slope.dy, 2 ) )
      val xlen = slope.dx * ( this.length / c )
      val xleft = xpos - ( xlen / 2 )
      val xright = xpos + ( xlen / 2 )
      p.right._1 >= xleft && p.left._1 <= xright
    }

    /** Stops a Mobile with downward momentum colliding with this Surface */
    val stopDown: Adjust = {
      case Moved( ar, p, m ) if isLanding( p.feet, m ) && inBounds( p ) ⇒
        val yintersect = slope.m * p.x + b
        Moved( ar, Position( p.x, yintersect + ( p.y - p.feet._2 ) ), Movement( m.x, 0 ) )
    }

    /** Moves a Mobile up along the Surface's slant if the Mobile is moving into the Surface */
    val moveup: Adjust = {
      case e @ Moved( ar, p, mv ) if mv.x * slope.m >= 0 && isLanding( p.feet, mv ) && inBounds( p ) ⇒
        val yintersect = slope.m * p.x + b

        e
    }

    adjusts = List( stopDown, moveup )
  }

  case class Wall( val xpos: Int,
                   val ypos: Int,
                   val length: Int ) extends Surface {
    val slope = Undefined
    val ytop = ypos + ( length / 2 )
    val ybottom = ypos - ( length / 2 )

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

  case class SingleSided( val xpos: Int,
                          val ypos: Int,
                          val length: Int,
                          val slope: Defined ) extends Floor

  case class DoubleSided( val xpos: Int,
                          val ypos: Int,
                          val length: Int,
                          val slope: Defined ) extends Floor

}