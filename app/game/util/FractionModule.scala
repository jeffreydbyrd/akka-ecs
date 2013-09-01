package game.util

trait FractionModule {

  implicit def intToFrac( i: Int ): Fraction = FracInt( i )
  implicit def fracToInt( f: FracInt ): Int = f.i

  def gcd( a: Int, b: Int ): Int = if ( b == 0 ) a else gcd( b, a % b )

  sealed trait Fraction {
    val toDouble: Double
    val isDefined: Boolean
    def :/( that: Fraction ) = Frac( this, that )
    def /( that: Fraction ) = this :/ that
  }

  sealed case class Frac( private val n: Fraction, private val d: Fraction ) extends Fraction {
    val isDefined = d != FracInt( 0 )
    override lazy val toDouble: Double = n.toDouble / d.toDouble
    override def toString() = s"( ${n.toString} / ${d.toString} )"
  }

  sealed case class FracInt( val i: Int ) extends Fraction {
    val isDefined = true
    override lazy val toDouble: Double = i.toDouble
    override def toString() = i.toString
  }

}