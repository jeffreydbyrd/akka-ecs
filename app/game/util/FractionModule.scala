package game.util

trait FractionModule {

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
    def +( that: Fraction ): Fraction = Fraction( this.n * that.d + that.n * this.d, this.d * that.d )
    def -( that: Fraction ): Fraction = this + ( -1 * that )
    def <( that: Fraction ): Boolean = ( this.n * that.d ) < ( that.n * this.d )
    def >( that: Fraction ): Boolean = ( this.n * that.d ) > ( that.n * this.d )
    def <=( that: Fraction ): Boolean = ( this.n * that.d ) <= ( that.n * this.d )
    def >=( that: Fraction ): Boolean = ( this.n * that.d ) >= ( that.n * this.d )
    def ==( that: Fraction ): Boolean = ( this.n * that.d ) == ( that.n * this.d )
  }

  def pow( f: Fraction, e: Int ): Fraction = {
    def powrec( acc: Fraction, c: Int ): Fraction = if ( c == e ) acc else powrec( f * acc, c + 1 )
    powrec( f, 1 )
  }
  
  //  def hypot(f1:Fraction, f2:Fraction):Fraction = sqrt

}