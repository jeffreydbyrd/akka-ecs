package game.util.math

trait ExpressionModule {

  /**
   * A mathematical expression that delays evaluation but can still perform
   * operations and comparisons
   */
  trait Expression {
    def *( that: Expression ): Expression
    def <( that: Expression ): Boolean
  }

  implicit def intToFraction( i: Int ): Fraction = Fraction( i, 1 )

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
    def /( that: Fraction ) = this :/ that
    def +( that: Fraction ): Fraction = ( this.n * that.d + that.n * this.d ) :/ ( this.d * that.d )
    def -( that: Fraction ): Fraction = this + ( -1 * that )
    def <( that: Fraction ): Boolean = ( this.n * that.d ) < ( that.n * this.d )
    def >( that: Fraction ): Boolean = ( this.n * that.d ) > ( that.n * this.d )
    def <=( that: Fraction ): Boolean = ( this.n * that.d ) <= ( that.n * this.d )
    def >=( that: Fraction ): Boolean = ( this.n * that.d ) >= ( that.n * this.d )
    def ==( that: Fraction ): Boolean = ( this.n * that.d ) == ( that.n * this.d )
    def !=( that: Fraction ): Boolean = !( this == that )

    /** Raises this Fraction to the 'e' power */
    def **( e: Int ): Fraction = ( 1 until e ).foldLeft( this )( ( f, c ) ⇒ f * this )
  }

  case class Sqrt( val f: Fraction ) {
    lazy val toDouble: Double = scala.math.sqrt( f.toDouble )
    def *( that: Sqrt ) = Sqrt( this.f * that.f )
    def *( f0: Fraction ) = Sqrt( ( f ** 2 ) * f )
    def /( that: Sqrt ) = Sqrt( this.f :/ that.f )
    def /( f0: Fraction ) = Sqrt( this.f :/ ( f0 ** 2 ) )
    def >( that: Sqrt ) = this.f > that.f
    def <( that: Sqrt ) = this.f < that.f
    def <=( that: Sqrt ) = this.f <= that.f
    def >=( that: Sqrt ) = this.f >= that.f
    def ==( that: Sqrt ) = this.f == that.f
    def !=( that: Sqrt ) = this.f != that.f
  }

}