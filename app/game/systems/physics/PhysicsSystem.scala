package game.systems.physics

import akka.actor.Props
import game.components.ComponentType.Input
import game.components.ComponentType.Position
import game.components.ComponentType.Velocity
import game.core.Engine.Tick
import game.entity.Entity
import game.systems.System
import game.systems.System.UpdateEntities

object PhysicsSystem {
  def props = Props( classOf[ PhysicsSystem ] )

  // Received
  case class CreateStructure( x: Float, y: Float, w: Float, h: Float )
  case class CreateMobile( x: Float, y: Float, w: Float, h: Float )
}

class PhysicsSystem( gx: Int, gy: Int ) extends System {
  import PhysicsSystem._

  val simulation = context.actorOf( Box2dSimulation.props, "simulation" )

  val mobileComponents = List( Input, Position, Velocity )
  var structures: Set[ Entity ] = Set()
  var mobiles: Set[ Entity ] = Set()
  var version = 0L

  override def receive = {
    case UpdateEntities( v, ents ) if v < version ⇒
      version = v
      var newStructs: Set[ Entity ] = Set()
      var newMobiles: Set[ Entity ] = Set()
      for ( e ← ents ) {
        if ( e.hasComponents( mobileComponents ) ) newMobiles += e
        val comps = e.components
        if ( comps.contains( Position ) && !comps.contains( Velocity ) )
          newStructs += e
      }

    case Tick ⇒
  }
}