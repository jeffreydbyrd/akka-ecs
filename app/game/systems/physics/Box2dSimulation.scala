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
import engine.entity.Entity
import game.components.io.InputComponent

class Box2dSimulation( gx: Int, gy: Int ) {

  private val timestep = 1.0f / 10.0f
  private val velocityIterations = 6
  private val positionIterations = 2

  var b2Mobiles: Map[ Entity, Box2dMobile ] = Map()

  // create a box2d world
  private val world = new World( new Vec2( gx, gy ) )
  private val contactListener = new Box2dContactListener
  world.setAllowSleep( true )
  world.setContactListener( contactListener )

  def add( sd: StructData ): Body = sd match {
    case StructData( ent, p, Rect( w, h ) ) =>
      createStructure( p.x, p.y, w, h )
  }

  def rem( e: Entity ) = {
    val mobile = b2Mobiles( e )
    contactListener.feet -= mobile.feet
    b2Mobiles -= e
    world.destroyBody( mobile.body )
  }

  def applyInputs( e: Entity, snap: InputComponent.Snapshot ) = {
    val m: Box2dMobile = b2Mobiles( e )
    if ( !( snap.left ^ snap.right ) ) m.setSpeed( 0 )
    else if ( snap.left ) m.setSpeed( -m.speed )
    else if ( snap.right ) m.setSpeed( m.speed )

    if ( snap.jump && m.remainingJumpSteps > 0 ) {
      m.jump()
      m.remainingJumpSteps -= 1
    }

    if ( !snap.jump && m.remainingJumpSteps < Box2dMobile.maxJumpSteps ) {
      m.remainingJumpSteps = 0
    }
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

  def createMobile( md: MobileData ): Box2dMobile = md match {
    case MobileData( entity, position, r: Rect, speed, hops ) =>
      // Define our body
      val mobileBodyDef = new BodyDef
      mobileBodyDef.fixedRotation = true
      mobileBodyDef.position.set( position.x, position.y )
      mobileBodyDef.`type` = BodyType.DYNAMIC
      val body: Body = world.createBody( mobileBodyDef )

      // Define main fixture shape
      val b2Shape = new PolygonShape
      b2Shape.setAsBox( r.w / 2, r.h / 2 )

      // Define body fixture
      val fixtureDef = new FixtureDef
      fixtureDef.shape = b2Shape
      fixtureDef.density = 1
      body.createFixture( fixtureDef )

      // Define foot sensor
      b2Shape.setAsBox( r.w / 2.1F, 0.1F, new Vec2( 0, -( r.h / 2 ) ), 0 )
      fixtureDef.isSensor = true
      fixtureDef.density = 0
      val footFixture = body.createFixture( fixtureDef )

      val mobile = new Box2dMobile( speed, hops, body, footFixture )
      contactListener.feet += footFixture -> mobile
      b2Mobiles += entity -> mobile
      mobile
  }

}