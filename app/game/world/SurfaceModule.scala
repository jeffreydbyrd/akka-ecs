package game.world

import scala.math._
import game.EventModule
import game.mobile.MobileModule
import game.util.math.LineModule
import java.math.MathContext
import java.math.RoundingMode

/**
 * A surface is an object with length, slope, and position. A surface
 * can be either a Wall or a Floor.
 *
 * A Wall always has a vertical (undefined) slope, while a Surface
 * always has a Defined slope.
 */
trait SurfaceModule extends LineModule with EventModule {
  this: RoomModule with MobileModule ⇒

  /**
   * A Surface is essentially just a line, owned by Room objects, that supplies Adjust
   * functions to modify Mobile Movements
   */
  trait Surface extends Line with AdjustSupplier {
    implicit val rm = BigDecimal.RoundingMode.HALF_UP

    def inBounds( p: PointLike ): Boolean =
      ( p.x.setScale( 5, rm ) between start.x.setScale( 5, rm ) -> end.x.setScale( 5, rm ) ) &&
        ( p.y.setScale( 5, rm ) between start.y.setScale( 5, rm ) -> end.y.setScale( 5, rm ) )
  }

  trait Floor extends Surface {
    def aboveFloor( p: PointLike ): Boolean = p.y > slope.m * p.x + b
    def belowFloor( p: PointLike ): Boolean = p.y < slope.m * p.x + b
    def onFloor( p: PointLike ): Boolean =
      p.y.setScale( 5, rm ) == ( slope.m * p.x + b ).setScale( 5, rm ) &&
        inBounds( p )

    val onCollision: Adjust = {
      // Mobile is standing on Floor and wants to move below it:
      case Moved( ar, p, mv ) if {
        onFloor( p.feet ) && belowFloor( Point( p.feet.x + mv.x, p.feet.y + mv.y ) )
      } ⇒
        val newMv =
          if ( mv.x == 0 ) Movement( 0, 0 )
          else {
            val k = hypot( slope.dx.toDouble, slope.dy.toDouble ) / mv.x
            Movement( slope.dx / k, slope.dy / k )
          }
        Moved( ar, p, newMv )

      // Mobile is above the Floor and wants to move below it:
      case Moved( ar, p, mv ) if {
        aboveFloor( p.feet ) && belowFloor( Point( p.feet.x + mv.x, p.feet.y + mv.y ) )
      } ⇒
        val vector = Vector( start = Point( p.feet.x, p.feet.y ), end = Point( p.feet.x + mv.x, p.feet.y + mv.y ) )
        val inter = Intersection( vector, this )
        val newMovement = if ( inBounds( inter ) ) Movement( inter.x - p.x, inter.y - p.feet.y ) else mv
        Moved( ar, p, newMovement )
    }

    adjusts = List( onCollision )
  }

  case class Wall( xpos: Int,
                   ytop: Int,
                   ybottom: Int ) extends Surface {
    lazy val start = Point( xpos, ytop )
    lazy val end = Point( xpos, ybottom )

    val stop: Adjust = {
      case Moved( ar, p, m ) if inBounds( p.left ) || inBounds( p.right ) ⇒
        Moved( ar, p, Movement( 0, m.y ) )
    }

    //    adjusts = adjusts ::: List( stopLeft, stopRight )
  }

  case class SingleSided( val start: Point, val end: Point ) extends Floor

  case class DoubleSided( val start: Point, val end: Point ) extends Floor
}