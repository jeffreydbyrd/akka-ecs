package game.util.math

class UndefinedSlopeException extends Exception

/** A convenience object for quickly creating Slopes */
object Slope {
  def apply( dx: BigDecimal, dy: BigDecimal ) =
    if ( dx == 0 ) Undefined
    else if ( dy == 0 ) Flat
    else Slant( dx, dy )
}

trait Slope {
  def dx: BigDecimal
  def dy: BigDecimal
  lazy val m = dy / dx
  def isDefined: Boolean
}

abstract class Defined( val dx: BigDecimal, val dy: BigDecimal ) extends Slope {
  override val isDefined = true
}

case class Slant( private val _x: BigDecimal, private val _y: BigDecimal ) extends Defined( _x, _y )

case object Flat extends Defined( 1, 0 )

case object Undefined extends Slope {
  val dx: BigDecimal = 0
  def dy = throw new UndefinedSlopeException
  override val isDefined = false
}

