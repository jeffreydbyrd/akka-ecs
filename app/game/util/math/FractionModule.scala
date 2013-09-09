package game.util.math

trait FractionModule {

  implicit def int2Fract( i: Int ): Fraction = Fraction( i, 1 )
  implicit def fract2BigD( f: Fraction ): BigDecimal = f.toBigD

  def gcd( a: Int, b: Int ): Int = if ( b == 0 ) a else gcd( b, a % b )

  /**
   * A simple data structure that delays evaluation while still allowing for operations and
   * comparisons. This structure is best used for avoiding decimal precision in systems
   * where decimal exactness is needed (something Doubles and Longs can't do).
   */
  case class Fraction( private val n: Int, private val d: Int ) {
    val toBigD: BigDecimal = BigDecimal( n ) / BigDecimal( d )
    val isDefined: Boolean = d != 0
    override def toString = s"$n / $d"

    def reduce: Fraction = {
      val k = gcd( n, d )
      Fraction( ( n / k ), ( d / k ) )
    }

    def *( that: Fraction ): Fraction = Fraction( n * that.n, d * that.d )
    def :/( that: Fraction ) = this * Fraction( that.d, that.n )
    def +( that: Fraction ): Fraction = Fraction( n * that.d + that.n * d, d * that.d )
    def -( that: Fraction ): Fraction = this + ( -1 * that )

    def ==( that: Fraction ): Boolean = ( n * that.d ) == ( that.n * d )
    def !=( that: Fraction ): Boolean = !( this == that )
    def >( that: Fraction ): Boolean = ( n * that.d ) > ( that.n * d )
    def <( that: Fraction ): Boolean = ( n * that.d ) < ( that.n * d )
    def >=( that: Fraction ): Boolean = !( this < that )
    def <=( that: Fraction ): Boolean = !( this > that )
  }
}