package game.util.math

trait ExpressionModule {

  /**
   * A mathematical expression that delays evaluation but can still perform
   * operations and comparisons
   */
  trait Expression {
    def eval: Double
    def *( that: Expression ): Expression
    def /( that: Expression ): Expression
    def +( that: Expression ): Expression
    def -( that: Expression ): Expression
    def >( that: Expression ): Boolean
    def <( that: Expression ): Boolean = !( this >= that )
    def >=( that: Expression ): Boolean = ( this > that ) || ( this == that )
    def <=( that: Expression ): Boolean = ( this < that ) || ( this == that )
    def ==( that: Expression ): Boolean
    def !=( that: Expression ): Boolean = !( this == that )
  }

  case class Composed( exp0: Expression, exp1: Expression ) extends Expression {
    def eval = exp0.eval + exp1.eval
    def *( that: Expression ): Composed = Composed( exp0 * that, exp1 * that )
    def /( that: Expression ): Composed = Composed( exp0 / that, exp1 / that )
    def +( that: Expression ): Composed = Composed( this, that )
    def -( that: Expression ): Composed = Composed( this, that * -1 )
    def >( that: Expression ): Boolean = this.eval > that.eval
    def ==( that: Expression ): Boolean = this.eval == that.eval
  }

  implicit def intToFraction( i: Int ): Fraction = Fraction( i, 1 )

  def gcd( a: Int, b: Int ): Int = if ( b == 0 ) a else gcd( b, a % b )

  case class Fraction( private val n: Int, private val d: Int ) extends Expression {
    val eval: Double = n.toDouble / d.toDouble
    val isDefined: Boolean = d != 0
    override def toString = s"$n / $d"
    def reduce: Fraction = {
      val k = gcd( n, d )
      ( ( n / k ) :/ ( d / k ) )
    }
    def *( that: Fraction ): Fraction = Fraction( n * that.n, d * that.d )
    def *( that: Sqrt ): Sqrt = Sqrt( this * this ) * that
    def :/( that: Fraction ) = this * Fraction( that.d, that.n )
    def /( that: Fraction ) = this :/ that
    def /( that: Sqrt ): Sqrt = Sqrt( this * this ) / that
    def +( that: Fraction ): Fraction = ( this.n * that.d + that.n * this.d ) :/ ( this.d * that.d )
    def -( that: Fraction ): Fraction = this + ( -1 * that )
    def >( that: Fraction ): Boolean = ( this.n * that.d ) > ( that.n * this.d )
    def ==( that: Fraction ): Boolean = ( this.n * that.d ) == ( that.n * this.d )

    def *( that: Expression ): Expression = that match {
      case f: Fraction ⇒ this * f
      case sq: Sqrt    ⇒ this * sq
      case c: Composed ⇒ c * this
    }
    def /( that: Expression ): Expression = that match {
      case f: Fraction ⇒ this / f
      case sq: Sqrt    ⇒ this / sq
      case c: Composed ⇒ c / this
    }
    def +( that: Expression ): Expression = that match {
      case f: Fraction ⇒ this + f
      case sq: Sqrt    ⇒ sq + this
      case c: Composed ⇒ c + this
    }
    def -( that: Expression ): Expression = this + ( that * -1 )
    def >( that: Expression ): Boolean = that match {
      case f: Fraction ⇒ this > f
      case sq: Sqrt    ⇒ that <= this
      case c: Composed ⇒ this > c
    }
    def ==( that: Expression ): Boolean = that match {
      case f: Fraction ⇒ this == that
      case sq: Sqrt    ⇒ that == this
      case c: Composed ⇒ c == this
    }

    /** Raises this Fraction to the 'e' power */
    def **( e: Int ): Fraction = ( 1 until e ).foldLeft( this )( ( f, c ) ⇒ f * this )
  }

  case class Sqrt( val f: Fraction ) extends Expression {
    lazy val eval: Double = scala.math.sqrt( f.eval )
    def *( that: Sqrt ) = Sqrt( this.f * that.f )
    def *( f0: Fraction ) = Sqrt( ( f ** 2 ) * f )
    def /( that: Sqrt ) = Sqrt( this.f :/ that.f )
    def /( f0: Fraction ) = Sqrt( this.f :/ ( f0 ** 2 ) )
    def >( that: Sqrt ) = this.f > that.f
    def ==( that: Sqrt ) = this.f == that.f

    def *( that: Expression ): Expression = that match {
      case f0: Fraction ⇒ this * f0
      case sq: Sqrt     ⇒ this * sq
      case c: Composed  ⇒ c * this
    }
    def /( that: Expression ): Expression = that match {
      case f0: Fraction ⇒ this / f0
      case sq: Sqrt     ⇒ this / sq
      case c: Composed  ⇒ c / this
    }
    def +( that: Expression ): Composed = Composed( this, that )
    def -( that: Expression ): Composed = Composed( this, -1 * that )
    def >( that: Expression ): Boolean = that match {
      case f0: Fraction ⇒ this > Sqrt( f0 * f0 )
      case sq: Sqrt     ⇒ this > sq
      case c: Composed  ⇒ c <= this
    }
    def ==( that: Expression ): Boolean = that match {
      case f0: Fraction ⇒ this == Sqrt( f0 * f0 )
      case sq: Sqrt     ⇒ this == sq
      case c: Composed  ⇒ c == this
    }
  }

}