package engine.util.math

/** Anything with an (x,y) position */
trait PointLike {
  val x: BigDecimal
  val y: BigDecimal
}

case class Point( val x: BigDecimal, val y: BigDecimal ) extends PointLike

class Intersection( l1: Line, l2: Line ) extends PointLike {
  require( l1.slope != l2.slope, "l1 and l2 must not be parallel" )

  lazy val ( x, y ) =
    if ( !l1.slope.isDefined )
      ( l1.start.x, l2.slope.m * l1.start.x + l2.b )
    else if ( !l2.slope.isDefined )
      ( l1.slope.m * l2.start.x + l1.b, l2.start.x )
    else {
      val x = ( l2.b - l1.b ) / ( l1.slope.m - l2.slope.m )
      ( x, l2.slope.m * x + l2.b )
    }
}