package game.util

trait FractionModule {

  implicit def intToFrac( i: Int ): FracInt = FracInt( i )
  implicit def fracToInt( f: FracInt ): Int = f.i

  def gcd( a: Int, b: Int ): Int = if ( b == 0 ) a else gcd( b, a % b )

  sealed trait Fraction {
    val toDouble: Double
    val isDefined: Boolean
    def :/( that: Fraction ) = Frac( this, that )
    def /( that: Fraction ) = this :/ that
    def *[ A <: Fraction ]( that: A ): Fraction
    def +[ A <: Fraction ]( that: A ): Fraction
    def -[ A <: Fraction ]( that: A ): Fraction = this + ( -1 * that )
  }

  case class Frac( private val n: Fraction, private val d: Fraction ) extends Fraction {
    val isDefined = d != FracInt( 0 )
    override lazy val toDouble: Double = n.toDouble / d.toDouble
    override def toString() = s"( ${n.toString} / ${d.toString} )"

    override def *[ A <: Fraction ]( that: A ) = that match {
      case Frac( _n, _d ) ⇒ ( this.n * _n ) :/ ( this.d * _d )
      case _: FracInt     ⇒ that * this
    }

    override def +[ A <: Fraction ]( that: A ) = that match {
      case Frac( _n, _d ) ⇒ ( this.n * _d + _n * this.d ) :/ ( this.d * _d )
      case _: FracInt     ⇒ that + this
    }
  }

  case class FracInt( val i: Int ) extends Fraction {
    val isDefined = true
    override lazy val toDouble: Double = i.toDouble
    override def toString() = i.toString

    override def *[ A <: Fraction ]( that: A ) = that match {
      case Frac( _n, _d ) ⇒ ( this * _n ) :/ _d
      case FracInt( _i )  ⇒ this.i * _i
    }

    override def +[ A <: Fraction ]( that: A ) = that match {
      case Frac( _n, _d ) ⇒ ( ( this * _d ) + _n ) :/ _d
      case FracInt( _i )  ⇒ this.i + _i
    }
  }

}