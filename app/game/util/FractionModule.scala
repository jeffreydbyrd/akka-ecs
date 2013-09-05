package game.util

trait FractionModule {

  implicit def intToFract( i: Int ): Fraction = Fraction( i, 1 )

  def gcd( a: Int, b: Int ): Int = if ( b == 0 ) a else gcd( b, a % b )

  case class Fraction( private val n: Int, private val d: Int ) {
    val toDouble: Double = n.toDouble / d.toDouble
    val isDefined: Boolean = d != 0
    override def toString = s"$n / $d"
    def reduce: Fraction = {
      val k = gcd( n, d )
      ( ( n / k ) :/ ( d / k ) )
    }
    def *( that: Fraction ): Fraction = Fraction( n * that.n, d * that.d )
    def :/( that: Fraction ) = this * Fraction( that.d, that.n )
    def +( that: Fraction ): Fraction = Fraction( this.n * that.d + that.n * this.d, this.d * that.d )
    def -[ A <: Fraction ]( that: A ): Fraction = this + ( -1 * that )
  }
}