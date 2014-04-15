package game.systems.physics

import org.jbox2d.callbacks.ContactListener
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.collision.Manifold
import org.jbox2d.dynamics.Fixture
import game.entity.Entity

class Box2dContactListener extends ContactListener {

  var feet: Map[ Fixture, Box2dMobile ] = Map()

  def beginContact( contact: Contact ): Unit =
    for {
      f ← List( contact.getFixtureA, contact.getFixtureB )
      m ← feet.get( f )
    } {
      println( "Grounded!" )
      m.grounded = true
    }

  def endContact( contact: Contact ): Unit = {
    val fixA = contact.getFixtureA
    val fixB = contact.getFixtureB
    feet.get( fixA ) foreach { m ⇒ m.grounded = false; println( "NOT grounded!" ) }
    feet.get( fixB ) foreach { m ⇒ m.grounded = false; println( "NOT grounded!" ) }
  }

  def postSolve( contact: Contact, impulse: ContactImpulse ): Unit = {}
  def preSolve( contact: Contact, manifold: Manifold ): Unit = {}
}