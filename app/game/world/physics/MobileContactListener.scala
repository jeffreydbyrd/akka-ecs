package game.world.physics

import org.jbox2d.callbacks.ContactListener
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.collision.Manifold

class MobileContactListener extends ContactListener {
  def firstNonNull[ T ]( ts: T* ): T = {
    for ( t ← ts.find( t ⇒ t != null ) ) return t
    throw new NullPointerException
  }

  override def beginContact( contact: Contact ): Unit =
    firstNonNull( contact.getFixtureA().getUserData(), contact.getFixtureB().getUserData() ) match {
      case mob: Mobile ⇒ mob.floorsTouched += 1
    }

  override def endContact( contact: Contact ): Unit =
    firstNonNull( contact.getFixtureA().getUserData(), contact.getFixtureB().getUserData() ) match {
      case mob: Mobile ⇒ mob.floorsTouched -= 1
    }

  override def postSolve( contact: Contact, impulse: ContactImpulse ): Unit = {}
  override def preSolve( contact: Contact, oldManifold: Manifold ): Unit = {}
}