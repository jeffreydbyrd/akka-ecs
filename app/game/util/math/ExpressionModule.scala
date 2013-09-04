import scala.math._

trait Scratch {

  /**
   * A mathematical expression that delays evaluation but can still perform
   * operations and comparisons
   */
  trait Expression {
    def eval: Double
    def *(that: Expression): Expression
    def /(that: Expression): Expression
    def +(that: Expression): Expression
    def -(that: Expression): Expression
    def >(that: Expression): Boolean
    def <(that: Expression): Boolean = !(this >= that)
    def >=(that: Expression): Boolean = (this > that) || (this == that)
    def <=(that: Expression): Boolean = (this < that) || (this == that)
    def ==(that: Expression): Boolean
    def !=(that: Expression): Boolean = !(this == that)
  }

  case class Composed(exp0: Expression, exp1: Expression) extends Expression {
    def eval = exp0.eval + exp1.eval
    def *(that: Expression): Composed = Composed(exp0 * that, exp1 * that) //distributive
    def /(that: Expression): Composed = Composed(exp0 / that, exp1 / that) //distributive
    def +(that: Expression): Composed = Composed(this, that) // (a + b) + c == a + (b + c)
    def -(that: Expression): Composed = Composed(this, that * -1) //(a + b) - c == (a + b) + (-1 * c)
    def >(that: Expression): Boolean = this.eval > that.eval
    def ==(that: Expression): Boolean = this.eval == that.eval
  }

  implicit def intToFraction(i: Int): Fraction = Fraction(i, 1)

  case class Fraction(private val n: Int, private val d: Int) extends Expression {
    val eval: Double = n.toDouble / d.toDouble
    val isDefined: Boolean = d != 0
    override def toString = s"$n / $d"
    def gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
    def reduce: Fraction = {
      val k = gcd(n, d)
      ((n / k) :/ (d / k))
    }
    def *(that: Fraction): Fraction = Fraction(n * that.n, d * that.d)
    def :/(that: Fraction) = { this * Fraction(that.d, that.n) }

    def *(that: Expression): Expression = that match {
      case f: Fraction => (n * f.n) :/ (d * f.d)
      case sq: Sqrt => Sqrt(this * this) * sq
      case c: Composed => c * this
    }
    def /(that: Expression): Expression = that match {
      case f: Fraction => Sqrt(this * this) / f
      case sq: Sqrt => Sqrt(this * this) / sq
      case c: Composed => Composed(this, 0) / c
    }
    def +(that: Expression): Expression = that match {
      case f: Fraction => (this.n * f.d + f.n * this.d) :/ (this.d * f.d)
      case sq: Sqrt => sq + this
      case c: Composed => c + this
    }
    def -(that: Expression): Expression = this + (-1 * that)
    def >(that: Expression): Boolean = that match {
      case f: Fraction => (this.n * f.d) > (f.n * this.d)
      case sq: Sqrt => sq <= this
      case c: Composed => c <= this
    }
    def ==(that: Expression): Boolean = that match {
      case f: Fraction => (this.n * f.d) == (f.n * this.d)
      case sq: Sqrt => sq == this
      case c: Composed => c == this
    }

    /** Raises this Fraction to the 'e' power */
    def **(e: Int): Fraction = (1 until e).foldLeft(this)((f, c) => f * this)
  }

  case class Sqrt(val f: Fraction) extends Expression {
    lazy val eval: Double = scala.math.sqrt(f.eval)

    def *(that: Expression): Expression = that match {
      case f0: Fraction => Sqrt((f ** 2) * f)
      case sq: Sqrt => Sqrt(this.f * sq.f)
      case c: Composed => c * this
    }
    def /(that: Expression): Expression = that match {
      case f0: Fraction => Sqrt(this.f :/ (f0 * f0))
      case sq: Sqrt => Sqrt(this.f :/ sq.f)
      case c: Composed => c / this
    }
    def +(that: Expression): Composed = Composed(this, that)
    def -(that: Expression): Composed = Composed(this, -1 * that)
    def >(that: Expression): Boolean = that match {
      case f0: Fraction => this > Sqrt(f0 * f0)
      case sq: Sqrt => this.f > sq.f
      case c: Composed => c <= this
    }
    def ==(that: Expression): Boolean = that match {
      case f0: Fraction => this == Sqrt(f0 * f0)
      case sq: Sqrt => this.f == sq.f
      case c: Composed => c == this
    }
  }
}
