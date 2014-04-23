package game


import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask

import engine.core.Engine
import game.systems.{VisualSystem, QuitSystem}
import game.systems.physics.PhysicsSystem
import game.components.physics.DimensionComponent
import engine.component.ComponentType.Dimension
import engine.component.{ComponentType, ComponentConfig}
import scala.concurrent.duration._
import game.systems.connection.ConnectionSystem
import scala.concurrent.{Await, Future}
import engine.system.SystemConfig
import engine.entity.EntityConfig

object MyGame {
  private val sysConfigs: Set[SystemConfig] = Set(
    SystemConfig(ConnectionSystem.props, "connection_system"),
    SystemConfig(QuitSystem.props, "quit_system"),
    SystemConfig(VisualSystem.props, "visual_system"),
    SystemConfig(PhysicsSystem.props(0, -10), "physics_system")
  )

  private val walls: Set[(ComponentType, ComponentConfig)] = Set(
    Dimension -> new ComponentConfig(DimensionComponent.props(25, 1, 50, 1), "floor"),
    Dimension -> new ComponentConfig(DimensionComponent.props(0, 25, 1, 50), "left_wall"),
    Dimension -> new ComponentConfig(DimensionComponent.props(50, 25, 1, 50), "right_wall")
  )

  private val wallConfigs: Set[EntityConfig] = walls.map(Map(_))

  implicit val timeout: akka.util.Timeout = 1.second

  private val actorSystem: ActorSystem = akka.actor.ActorSystem("Doppelsystem")

  private val doppelengine: ActorRef =
    actorSystem.actorOf(Engine.props(sysConfigs), name = "engine")

  val connectionSystem = {
    val fConnSystem: Future[ActorRef] =
      actorSystem.actorSelection(doppelengine.path / "connection_system").resolveOne
    Await.result(fConnSystem, 1000 millis)
  }

  doppelengine ? Engine.Add(0, wallConfigs)
}
