package game.util.math

import scala.math._

trait RadicalModule {
  this: FractionModule ⇒

  case class Radical( val f: Fraction ) {
    lazy val calculate: Double = sqrt( f.toDouble )
    def *( that: Radical ) = Radical( this.f * that.f )
    def /( that: Radical ) = Radical( this.f :/ that.f )
  }

}