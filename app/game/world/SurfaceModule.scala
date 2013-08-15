package game.world

trait SurfaceModule {

  trait Slope
  case class Defined( x: Int, y: Int ) extends Slope
  case object Undefined extends Slope

  trait Surface {
    val xpos: Int
    val ypos: Int
    val length: Int
    val slope: Slope
  }

  trait Floor

  trait Wall {
    val slope = Undefined
  }

  trait SingleSided extends Floor

  trait DoubleSided extends Floor

}