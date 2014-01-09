package game.util.math

import scala.math.BigDecimal.int2bigDecimal

import org.scalatest.FunSuiteLike
import org.scalatest.Matchers

class LineSpec extends FunSuiteLike with Matchers {

  test( "Slope.m should return BigDec if the slope is a Slant" ) {
    Slant( 2, 2 ).m === 1.0
  }

  test( "Slope.m should return 0.0 if the slope is a Flat" ) {
    Flat.m === 0.0
  }

  test( "Slope.m should throw UndefinedSlopeException if Undefined" ) {
    an[ UndefinedSlopeException ] should be thrownBy Undefined.m
  }

  test( "The Slope companion object should be able to construct a Slope" ) {
    Slope( 0, 0 ) === Undefined
    Slope( 1, 0 ) === Flat
    Slope( 1, -1 ) === Slant( 1, -1 )
  }

  test( "Intersection should be able to represent the intersection of 2 slanted lines" ) {
    val l1 = Vector( Point( 0, 0 ), Point( 10, 10 ) )
    val l2 = Vector( Point( 0, 10 ), Point( 10, 0 ) )
    val int = new Intersection( l1, l2 )
    int.x === 5
    int.y === 5
  }

  test( "Intersection should be able to represent the intersection of a slanted line and vertical line" ) {
    val slanted = Vector( Point( 0, 0 ), Point( 10, 10 ) )
    val vertical = Vector( Point( 5, 0 ), Point( 5, 10 ) )
    val int1 = new Intersection( slanted, vertical )
    int1.x === 5
    int1.y === 5

    val int2 = new Intersection( vertical, slanted )
    int2.x === 5
    int2.y === 5
  }

  test( "Intersection should throw an IllegalArgumentException if both lines are parallel" ) {
    val l1 = Vector( Point( 0, 0 ), Point( 0, 10 ) )
    val l2 = Vector( Point( 1, 0 ), Point( 1, 10 ) )
    an[ IllegalArgumentException ] should be thrownBy ( new Intersection( l1, l2 ) )
  }
}