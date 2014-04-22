package game


import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask

import engine.core.Engine
import game.systems.{VisualSystem, QuitSystem}
import game.systems.physics.PhysicsSystem
import game.components.physics.DimensionComponent
import engine.component.ComponentType.Dimension
import engine.component.ComponentConfig
import engine.entity.EntityConfig
import scala.concurrent.duration._
import game.systems.connection.ConnectionSystem
import scala.concurrent.{Await, Future}

object MyGame {
  implicit val timeout: akka.util.Timeout = 1.second

  private val actorSystem: ActorSystem = akka.actor.ActorSystem("Doppelsystem")

  private val sysConfigs: Set[(Props, String)] = Set(
    (ConnectionSystem.props, "connection_system"),
    (QuitSystem.props, "quit_system"),
    (VisualSystem.props, "visual_system"),
    (PhysicsSystem.props(0, -10), "physics_system")
  )

  private val doppelengine: ActorRef =
    actorSystem.actorOf(Engine.props(sysConfigs), name = "engine")

  val connectionSystem = {
    val fConnSystem: Future[ActorRef] =
      actorSystem.actorSelection(doppelengine.path / "connection_system").resolveOne
    Await.result(fConnSystem, 1000 millis)
  }

  private val floorConfig: EntityConfig = Map(
    Dimension -> new ComponentConfig(DimensionComponent.props(25, 1, 50, 1), "floor")
  )

  doppelengine ? Engine.Add(0, Set(floorConfig))
}
