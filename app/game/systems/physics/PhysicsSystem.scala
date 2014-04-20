package game.systems.physics

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import akka.pattern.ask
import game.components.Component.RequestSnapshot
import game.components.ComponentType.Dimension
import game.components.ComponentType.Input
import game.components.ComponentType.Mobility
import game.components.io.InputComponent.Snapshot
import game.components.physics.DimensionComponent
import game.components.physics.MobileComponent
import game.components.physics.Position
import game.components.physics.Shape
import game.core.Engine.Tick
import game.core.Engine.TickAck
import game.core.Engine.timeout
import game.entity.Entity
import game.systems.System
import game.systems.System.UpdateEntities
import game.components.io.InputComponent

object PhysicsSystem {
  import game.components.physics.Shape
  import game.components.physics.Position

  def props( gx: Int, gy: Int ) = Props( classOf[ PhysicsSystem ], gx, gy )

  trait Data
  case class MobileData( e: Entity, p: Position, s: Shape, speed: Float, hops: Float ) extends Data
  case class StructData( e: Entity, p: Position, s: Shape )

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

  val mobileComponents = List( Input, Dimension, Mobility )
  val simulation = new Box2dSimulation( gx, gy )

  def updateEntities( addStructs: Set[ Future[ StructData ] ],
                      addMobiles: Set[ Future[ MobileData ] ],
                      remMobiles: Set[ Future[ MobileData ] ] ) = {
    for ( futureStruct ← addStructs )
      simulation.add( Await.result( futureStruct, 1000 millis ) )

    for ( futureMobile ← addMobiles ) {
      val data = Await.result( futureMobile, 1000 millis )
      simulation.createMobile( data )
    }

    for ( futureMobile ← remMobiles ) {
      val data = Await.result( futureMobile, 1000 millis )
      simulation.rem( data.e )
    }
  }

  override def receive = manage( 0, Set(), Set() )

  def manage( version: Long, structures: Set[ Entity ], mobiles: Set[ Entity ] ): Receive = LoggingReceive {
    case UpdateEntities( v, ents ) if v > version ⇒
      var newStructs: Set[ Entity ] = Set()
      var newMobiles: Set[ Entity ] = Set()

      for ( e ← ents ) {
        if ( e.hasComponents( mobileComponents ) ) newMobiles += e
        val comps = e.components
        if ( comps.contains( Dimension ) && !comps.contains( Mobility ) )
          newStructs += e
      }
      updateEntities( getStructData( newStructs -- structures ),
        getMobileData( newMobiles -- mobiles ),
        getMobileData( mobiles -- newMobiles ) )

      context.become( manage( v, newStructs, newMobiles ) )

    case Tick ⇒
      import InputComponent.Snapshot

      // Get inputs and apply them
      val futureSnaps: Set[ ( Entity, Future[ Snapshot ] ) ] =
        for ( e ← mobiles )
          yield ( e, ( e( Input ) ? RequestSnapshot ).mapTo[ Snapshot ] )

      for ( ( e, fs ) ← futureSnaps ) {
        simulation.applyInputs( e, Await.result( fs, 1000 millis ) )
      }

      simulation.step()

      // Update the components with new positions
      for ( ( e, b2Mob ) ← simulation.b2Mobiles ) {
        val x = b2Mob.body.getPosition.x
        val y = b2Mob.body.getPosition.y
        e( Dimension ) ! DimensionComponent.UpdatePosition( x, y )
      }

      sender ! TickAck
  }
}