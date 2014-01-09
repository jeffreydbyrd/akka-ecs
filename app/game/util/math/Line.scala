package game.util.math

import scala.math.BigDecimal
import scala.math.BigDecimal.int2bigDecimal

trait Line {
  val start: Point
  val end: Point
  lazy val slope = Slope( end.x - start.x, end.y - start.y )
  lazy val b = start.y - ( slope.m * start.x )
}

case class Vector( val start: Point, val end: Point ) extends Line
