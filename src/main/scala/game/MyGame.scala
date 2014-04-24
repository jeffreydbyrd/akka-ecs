package game

import engine.core.Engine
import engine.system.SystemConfig
import akka.actor.{ActorRef, ActorSystem}
import scala.concurrent.duration._

object MyGame extends App {
  private val sysConfigs: Set[SystemConfig] = Set(
  )

  implicit val timeout: akka.util.Timeout = 1.second

  private val actorSystem: ActorSystem = akka.actor.ActorSystem("Doppelsystem")

  private val doppelengine: ActorRef =
    actorSystem.actorOf(Engine.props(sysConfigs, 2.seconds), name = "engine")
}
