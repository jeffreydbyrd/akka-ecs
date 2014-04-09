package game.systems.physics

import akka.actor.Actor
import akka.actor.Props
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyType
import game.components.physics.Position
import scala.math.abs
import game.systems.physics.PhysicsSystem.MobileData
import game.systems.physics.PhysicsSystem.StructData
import game.components.physics.Rect
import game.entity.Entity

class Box2dSimulation( gx: Int, gy: Int ) {

  // Not really sure what these are for... all the tutorials use these values
  private val timestep = 1.0f / 50.0f
  private val velocityIterations = 6
  private val positionIterations = 2

  // create a box2d world
  private val world = new World( new Vec2( gx, gy ) )
  world.setAllowSleep( true )

  def add( sd: StructData ): Body = sd match {
    case StructData( ent, p, Rect( w, h ) ) ⇒
      createStructure( p.x, p.y, w, h )
  }

  def add( md: MobileData ): Box2dMobile = md match {
    case MobileData( ent, p, Rect( w, h ), speed, hops ) ⇒
      val body = createMobile( p.x, p.y, w, h )
      new Box2dMobile( speed, hops, body )
  }

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