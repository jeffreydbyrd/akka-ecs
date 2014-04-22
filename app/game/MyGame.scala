package game


import akka.actor.Props
import akka.pattern.ask

import engine.core.{Game, Engine}
import game.systems.{VisualSystem, QuitSystem}
import game.systems.physics.PhysicsSystem
import game.components.physics.DimensionComponent
import engine.component.ComponentType.Dimension
import engine.component.ComponentConfig
import engine.entity.EntityConfig
import scala.concurrent.duration._
import game.systems.connection.ConnectionSystem

class MyGame extends Game {
  implicit val timeout: akka.util.Timeout = 1.second

  override val sysConfigs: Set[(Props, String)] = Set(
    (ConnectionSystem.props, "connection_system"),
    (QuitSystem.props, "quit_system"),
    (VisualSystem.props, "visual_system"),
    (PhysicsSystem.props(0, -10), "physics_system")
  )

  private val floorConfig: EntityConfig = Map(
    Dimension -> new ComponentConfig(DimensionComponent.props(25, 1, 50, 1), "floor")
  )

  doppelengine ? Engine.Add(0, Set(floorConfig))
}
