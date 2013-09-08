package game.util.math

import scala.math._

trait LineModule {
  /* 
   * Just for fun:
   * Implicit conversions that basically give Ints/Doubles a 'between' function.
   * example: 5 between 0 -> 10 == true
   * example: 5 between 1 -> 3 == false
   */
  case class IntBetween( i: Int ) { def between( ns: ( Int, Int ) ) = ( ns._1 <= i && i <= ns._2 ) || ( ns._1 >= i && i >= ns._2 ) }
  implicit def intBetween( i: Int ) = IntBetween( i )
  case class DoubleBetween( d: Double ) { def between( ns: ( Double, Double ) ) = ( ns._1 <= d && d <= ns._2 ) || ( ns._1 >= d && d >= ns._2 ) }
  implicit def doublBetween( d: Double ) = DoubleBetween( d )

  class UndefinedSlopeException extends Exception

  trait Slope {
    def dx: Double
    def dy: Double
    lazy val m = dy / dx
    def isDefined: Boolean
  }

  abstract class Defined( val dx: Double, val dy: Double ) extends Slope {
    override val isDefined = true
  }

  case class Slant( private val _x: Double, private val _y: Double ) extends Defined( _x, _y )

  case object Flat extends Defined( 1, 0 )

  case object Undefined extends Slope {
    val dx: Double = 0
    def dy = throw new UndefinedSlopeException
    override val isDefined = false
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

  trait PointLike {
    val x: Double
    val y: Double
  }

  case class Point( val x: Double, val y: Double ) extends PointLike

  trait Line {
    val start: Point
    val end: Point
    lazy val slope = Slope( end.x - start.x, end.y - start.y )
    lazy val b = start.y - ( slope.m * start.x )

  }

  case class Vector( val start: Point, val end: Point ) extends Line

  case class Intersection( l1: Line, l2: Line ) extends PointLike {
    lazy val x =
      if ( l1.slope.isDefined )
        ( l2.b - l1.b ) / ( l1.slope.m - l2.slope.m )
      else
        l1.start.x
    lazy val y = l2.slope.m * x + l2.b
  }
}