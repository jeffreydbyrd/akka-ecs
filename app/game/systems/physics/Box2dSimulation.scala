package game.systems.physics

import akka.actor.Actor
import akka.actor.Props
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World
import org.jbox2d.common.Vec2
import game.world.physics.MobileContactListener
import org.jbox2d.dynamics.BodyType
import game.components.physics.Position
import game.world.physics.Simulation
import scala.math.abs
import game.systems.physics.PhysicsSystem.MobileData
import game.systems.physics.PhysicsSystem.StructData
import game.components.physics.Rect

class Box2dSimulation( gx: Int, gy: Int ) {

  // Not really sure what these are for... all the tutorials use these values
  val timestep = 1.0f / 60.0f
  val velocityIterations = 6
  val positionIterations = 2

  // create a box2d world
  val world = new World( new Vec2( gx, gy ) )
  world.setAllowSleep( true )
  //world.setContactListener( new MobileContactListener )

  var mobiles: Set[ Box2dMobile ] = Set()
  var structs: Set[ Body ] = Set()

  def add( data: Set[ PhysicsSystem.Data ] ) =
    data.foreach {
      case MobileData( ent, p, Rect( w, h ), speed, hops ) ⇒
        val body = createMobile( p.x, p.y, w, h )
        mobiles += new Box2dMobile( ent, speed, hops, body )

      case StructData( ent, p, Rect( w, h ) ) ⇒
        structs += createStructure( p.x, p.y, w, h )
    }

  def remove( data: Set[ PhysicsSystem.Data ] ) = { /*TODO*/ }

  def step() = {
    world.step( timestep, velocityIterations, positionIterations )
  }

  private def createStructure( x: Float, y: Float, w: Float, h: Float ): Body = {
    val bodyDef = new BodyDef
    bodyDef.position.set( x, y )
    bodyDef.`type` = BodyType.STATIC

    val blockShape = new PolygonShape
    blockShape.setAsBox( w / 2, h / 2 )

    val fixtureDef = new FixtureDef
    fixtureDef.shape = blockShape

    val body: Body = world.createBody( bodyDef )
    body.createFixture( fixtureDef )
    body
  }

  private def createMobile( x: Float, y: Float, w: Float, h: Float ): Body = {
    val mobileBodyDef = new BodyDef
    mobileBodyDef.fixedRotation = true
    mobileBodyDef.position.set( x, y )
    mobileBodyDef.`type` = BodyType.DYNAMIC

    val blockShape = new PolygonShape
    blockShape.setAsBox( w / 2, h / 2 )

    val fixtureDef = new FixtureDef
    fixtureDef.shape = blockShape
    fixtureDef.friction = 0

    val body: Body = world.createBody( mobileBodyDef )
    body.createFixture( fixtureDef )
    body
  }

}