package game.systems.physics

import akka.actor.Actor
import akka.actor.Props

object Box2dSimulation {
  def props = Props( classOf[ Box2dSimulation ] )
}

class Box2dSimulation extends Actor {
  override def receive = {
    case _ ⇒
  }
}