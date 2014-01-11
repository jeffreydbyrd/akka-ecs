package game.world

import org.scalatest.FunSuiteLike
import game.util.math.Point
import game.mobile.Position
import game.mobile.Movement
import game.mobile.Mobile._

class SurfaceSpec extends FunSuiteLike {

  test( "Surface.inBounds( p ) should respect the bounds of the Surface" ) {
    val surface = new Surface {
      val start = Point( 0, 0 )
      val end = Point( 10, 10 )
    }
    surface.inBounds( Point( 10, 10 ) ) === true
    surface.inBounds( Point( 5, 5 ) ) === true
    surface.inBounds( Point( 7, 7 ) ) === true

    surface.inBounds( Point( 11, 11 ) ) === false
    surface.inBounds( Point( 7, 11 ) ) === false
  }

  test( "Floor.onCollision redirect the Movement of a Mobile standing on Floor and moving into it" ) {
    val floor = new SingleSided( Point( 0, 0 ), Point( 10, 10 ) )
    val event = Moved( null, Position( 5, 7, 4, 2 ), Movement( 1, -1 ) )
    val newMv = ( floor.onCollision( event ) ).asInstanceOf[ Moved ].m
    ( 0 <= newMv.x && newMv.x <= 1 ) === true
    ( 0 <= newMv.y && newMv.y <= 1 ) === true
  }

  test( "Floor.onCollision should shorten the Movement of a Mobile falling into the Floor" ) {
    val floor = new SingleSided( Point( 0, 0 ), Point( 10, 10 ) )
    val event = Moved( null, Position( 0, 4, 4, 2 ), Movement( 2, -2 ) )
    val newMv = ( floor.onCollision( event ) ).asInstanceOf[ Moved ].m
    newMv.x === 1
    newMv.y === -1
  }

  test( "Floor.onCollision should be undefined for Movement that does not intersect with the Floor" ) {
    val floor = new SingleSided( Point( 0, 0 ), Point( 10, 10 ) )
    val event = Moved( null, Position( 0, 10, 4, 2 ), Movement( 2, -2 ) )
    floor.onCollision.isDefinedAt( event ) === false
  }

}