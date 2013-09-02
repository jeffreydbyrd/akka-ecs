package game.util

import scala.math._

trait LineModule extends FractionModule {
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
  case class FracBetween( i: Fraction ) { def between( ns: ( Fraction, Fraction ) ) = ( ns._1 <= i && i <= ns._2 ) || ( ns._1 >= i && i >= ns._2 ) }
  implicit def fracBetween( f: Fraction ) = FracBetween( f )

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
    override val dx: Double = 0
    override def dy = throw new UndefinedSlopeException
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

  case class Point( x: Double, y: Double )

  trait Line {
    val start: Point
    val end: Point
    lazy val length = hypot( start.x - end.x, start.y - end.y )
    lazy val slope = Slope( start.x - end.x, start.y - end.y )
    lazy val b = start.y - ( slope.m * start.x )

    protected case class Intersection( val that: Line ) {
      val x =
        if ( that.slope.isDefined )
          ( b - that.b ) / ( that.slope.m - slope.m )
        else
          that.start.x
      val y = slope.m * x + b
      private val startToIntercept = hypot( that.start.x - x, that.start.y - y ) // distance from start to intercept
      val isLanding =
        ( that.length >= startToIntercept ) &&
          ( x between start.x -> end.x ) &&
          ( y between start.y -> end.y )
    }
  }

  case class Vector( val start: Point, val end: Point ) extends Line

}