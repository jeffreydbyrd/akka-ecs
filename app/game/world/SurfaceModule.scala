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
    def isLanding( p: Position, mv: Movement ) = {
      val v = Vector( Point( p.feet._1, p.feet._2 ), Point( p.feet._1 + mv.x, p.feet._2 + mv.y ) )
      val xinter = if (v.slope.isDefined) ( b - v.b ) / ( v.slope.m - slope.m ) else p.x
      val yinter = slope.m * xinter + b 
      val startToIntercept = hypot( v.start.x - xinter, v.start.y - yinter ) // distance from start to intercept
      println("==========================")
      println( s"xinter=$xinter yinter=$yinter startToIntercept=$startToIntercept" )
      println(s"startx=${start.x} starty=${start.y}, endx=${end.x}  endy=${end.y}")
      println(s"xfeet=${p.feet._1} yfeet=${p.feet._2} vlength=${v.length}")
      println("==========================")
      ( ( v.length >= startToIntercept ) &&
        ( xinter between start.x -> end.x ) &&
        ( yinter between start.y -> end.y ) )
    }

    /** Stops a Mobile whose Movement will intersect with this Surface */
    val onCollision: Adjust = {
      // this sucks: we calculate the Mobile's vector twice here:
      case Moved( ar, p, mv ) if isLanding( p, mv ) ⇒
        val v = Vector( Point( p.feet._1, p.feet._2 ), Point( start.x + mv.x, start.y + mv.y ) )
      val xinter = if (v.slope.isDefined) ( b - v.b ) / ( v.slope.m - slope.m ) else p.x
      val yinter = slope.m * xinter + b 
        val newMovement =
          if ( mv.x * slope.m >= 0 && ( p.x == xinter && p.feet._2 == yinter ) ) {
            val k = hypot( slope.dx, slope.dy ) / mv.x
            Movement( slope.dx / k, slope.dy / k )
          } else Movement( xinter - p.x, yinter - p.feet._2 )
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