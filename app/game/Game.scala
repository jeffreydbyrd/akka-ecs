package game


import akka.actor.{ActorSystem, Props, ActorRef}

import engine.core.Engine
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import game.systems.{VisualSystem, QuitSystem, ConnectionSystem}
import game.systems.physics.PhysicsSystem
import game.components.physics.DimensionComponent
import engine.component.ComponentType.Dimension
import engine.component.ComponentConfig
import engine.entity.EntityConfig

object Game {
  implicit val timeout: akka.util.Timeout = 1.second
  val system: ActorSystem = akka.actor.ActorSystem("Doppelsystem")

  private val sysConfigs: Set[(Props, String)] = Set(
    (ConnectionSystem.props, "connection_system"),
    (QuitSystem.props, "quit_system"),
    (VisualSystem.props, "visual_system"),
    (PhysicsSystem.props(0, -10), "physics_system")
  )


  val doppelengine: ActorRef =
    system.actorOf(Engine.props(sysConfigs), name = "engine")

  private val floorConfig: EntityConfig = Map(
    Dimension -> new ComponentConfig(DimensionComponent.props(25, 1, 50, 1), "floor")
  )

  doppelengine ! Engine.Add(0, Set(floorConfig))

  // Retrieve connection_system
  val connectionSystem = {
    println("setting connection system...")
    val fConnSystem: Future[ActorRef] =
      system.actorSelection("/user/engine/connection_system").resolveOne()
    Await.result(fConnSystem, 1000 millis)
  }
}
