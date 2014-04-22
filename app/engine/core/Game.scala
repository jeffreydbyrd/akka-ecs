package engine.core

import akka.actor.{ActorRef, Props, ActorSystem}

trait Game {
  val actorSystem: ActorSystem = akka.actor.ActorSystem("Doppelsystem")

  def sysConfigs: Set[(Props, String)]

  lazy val doppelengine: ActorRef =
    actorSystem.actorOf(Engine.props(sysConfigs), name = "engine")
}
