package game.systems.physics

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import akka.actor.Props
import akka.pattern.ask
import akka.pattern.pipe
import game.components.Component.RequestSnapshot
import game.components.ComponentType.Dimension
import game.components.ComponentType.Input
import game.components.ComponentType.Mobility
import game.components.physics.DimensionComponent
import game.components.physics.Position
import game.components.physics.Shape
import game.core.Engine.Tick
import game.core.Game.timeout
import game.entity.Entity
import game.systems.System
import game.systems.System.UpdateEntities
import game.components.physics.MobileComponent
import akka.event.LoggingReceive

object PhysicsSystem {
  import game.components.physics.Shape
  import game.components.physics.Position

  def props( gx: Int, gy: Int ) = Props( classOf[ PhysicsSystem ], gx, gy )

  trait Data
  case class MobileData( e: Entity, p: Position, s: Shape, speed: Float, hops: Float ) extends Data
  case class StructData( e: Entity, p: Position, s: Shape ) extends Data

  sealed case class Update(
    v: Long,
    newStructs: Set[ Entity ],
    newMobiles: Set[ Entity ],
    add: Set[ Data ],
    rem: Set[ Data ] )

  def getStructData( structs: Set[ Entity ] ): Set[ Future[ StructData ] ] =
    structs.map { e ⇒
      ( e( Dimension ) ? RequestSnapshot ).map {
        case s: DimensionComponent.Snapshot ⇒ StructData( e, s.pos, s.shape )
      }
    }

  def getMobileData( mobs: Set[ Entity ] ): Set[ Future[ MobileData ] ] =
    mobs.map( e ⇒ {
      val fDim = ( e( Dimension ) ? RequestSnapshot ).mapTo[ DimensionComponent.Snapshot ]
      val fMob = ( e( Mobility ) ? RequestSnapshot ).mapTo[ MobileComponent.Snapshot ]
      for ( dim ← fDim; mob ← fMob )
        yield MobileData( e, dim.pos, dim.shape, mob.speed, mob.hops )
    } )
}

class PhysicsSystem( gx: Int, gy: Int ) extends System {
  import PhysicsSystem._

  val simulation = new Box2dSimulation( gx, gy )

  val mobileComponents = List( Input, Dimension, Mobility )
  var structures: Set[ Entity ] = Set()
  var mobiles: Set[ Entity ] = Set()
  var version = 0L

  override def receive = LoggingReceive {
    case UpdateEntities( v, ents ) if v > version ⇒
      version = v
      var newStructs: Set[ Entity ] = Set()
      var newMobiles: Set[ Entity ] = Set()

      for ( e ← ents ) {
        if ( e.hasComponents( mobileComponents ) ) newMobiles += e
        val comps = e.components
        if ( comps.contains( Dimension ) && !comps.contains( Mobility ) )
          newStructs += e
      }

      val addFutures: Set[ Future[ Data ] ] =
        getStructData( newStructs -- structures ) ++ getMobileData( newMobiles -- mobiles )
      val remFutures: Set[ Future[ Data ] ] =
        getStructData( structures -- newStructs ) ++ getMobileData( mobiles -- newMobiles )

      for {
        add ← Future.sequence( addFutures )
        rem ← Future.sequence( remFutures )
      } self ! Update( v, newStructs, newMobiles, add, rem )

    case Update( v, newStructs, newMobiles, add, rem ) if v == version ⇒
      structures = newStructs
      mobiles = newMobiles
      simulation.add( add )
      simulation.remove( rem )

    case Tick ⇒
      simulation.step()
      simulation.mobiles.foreach { m ⇒
        val x = m.body.getPosition.x
        val y = m.body.getPosition.y
        m.entity( Dimension ) ! DimensionComponent.UpdatePosition( x, y )
      }
  }
}