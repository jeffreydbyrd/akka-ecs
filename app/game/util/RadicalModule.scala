package game.util

import scala.math._

trait RadicalModule {
  case class Radical( val i: Int ) {
    lazy val calculate = sqrt( i )
    def *( that: Radical ) = Radical( this.i * that.i )
  }

}