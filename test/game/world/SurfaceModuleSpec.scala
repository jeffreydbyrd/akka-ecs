package game.world

import org.specs2.mutable.Specification
import game.communications.ConnectionModule
import game.EventModule
import game.mobile.MobileModule
import game.mobile.PlayerModule
import game.GameModule

class SurfaceModuleSpec
    extends SurfaceModule
    with EventModule
    with MobileModule
    with RoomModule
    with PlayerModule
    with GameModule
    with ConnectionModule
    with Specification { // that's a lot of mixins

  override val system = null
  val GAME = null

  "Surface.inBounds(p)" should {
    "return true when p is within the X and Y bounds of a Surface" in {
      val surface = new Surface {
        val start = Point( 0, 0 )
        val end = Point( 10, 10 )
      }
      surface.inBounds( Point( 10, 10 ) ) must beTrue
      surface.inBounds( Point( 5, 5 ) ) must beTrue
      surface.inBounds( Point( 7, 7 ) ) must beTrue
    }

    "return false when p is outside the X and Y bounds of a Surface" in {
      val surface = new Surface {
        val start = Point( 0, 0 )
        val end = Point( 10, 10 )
      }
      surface.inBounds( Point( 11, 11 ) ) must beFalse
      surface.inBounds( Point( 7, 11 ) ) must beFalse
    }
  }

  "Floor.onCollision" should {
    "redirect the Movement of a Mobile standing on Floor and moving into it" in {
      val floor = new SingleSided( Point( 0, 0 ), Point( 10, 10 ) )
      val event = Moved( Position( 5, 7, 4, 2 ), Movement( 1, -1 ) )
      val newMv = floor.onCollision( event ).asInstanceOf[ Moved ].m
      ( newMv.x between BigDecimal( 0 ) -> 1 ) must beTrue
      ( newMv.y between BigDecimal( 0 ) -> 1 ) must beTrue
    }

    "shorten the Movement of a Mobile falling into the Floor" in {
      val floor = new SingleSided( Point( 0, 0 ), Point( 10, 10 ) )
      val event = Moved( Position( 0, 4, 4, 2 ), Movement( 2, -2 ) )
      val newMv = floor.onCollision( event ).asInstanceOf[ Moved ].m
      newMv.x === 1
      newMv.y === -1
    }

    "be undefined for Movement that does not intersect with the Floor" in {
      val floor = new SingleSided( Point( 0, 0 ), Point( 10, 10 ) )
      val event = Moved( Position( 0, 10, 4, 2 ), Movement( 2, -2 ) )
      floor.onCollision.isDefinedAt( event ) must beFalse
    }
  }

}