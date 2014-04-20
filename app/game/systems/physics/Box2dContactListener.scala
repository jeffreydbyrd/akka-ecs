package game.systems.physics

import org.jbox2d.callbacks.ContactListener
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.collision.Manifold
import org.jbox2d.dynamics.Fixture
import game.entity.Entity

class Box2dContactListener extends ContactListener {

  var feet: Map[ Fixture, Box2dMobile ] = Map()

  private def feetContact( landing: Boolean, c: Contact ) = for {
    f <- List( c.getFixtureA, c.getFixtureB )
    m <- feet.get( f )
  } {
    m.grounded = landing
    if ( landing ) {
      m.remainingJumpSteps = Box2dMobile.maxJumpSteps
    }
  }

  def beginContact( contact: Contact ): Unit = feetContact( true, contact )
  def endContact( contact: Contact ): Unit = feetContact( false, contact )

  def postSolve( contact: Contact, impulse: ContactImpulse ): Unit = {}
  def preSolve( contact: Contact, manifold: Manifold ): Unit = {}
}