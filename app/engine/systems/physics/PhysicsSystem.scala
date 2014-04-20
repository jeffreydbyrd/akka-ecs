package engine.systems.physics

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import akka.pattern.ask
import engine.components.Component.RequestSnapshot
import engine.components.ComponentType.Dimension
import engine.components.ComponentType.Input
import engine.components.ComponentType.Mobility
import engine.components.io.InputComponent.Snapshot
import engine.components.physics.DimensionComponent
import engine.components.physics.MobileComponent
import engine.components.physics.Position
import engine.components.physics.Shape
import engine.core.Engine.Tick
import engine.core.Engine.TickAck
import engine.core.Engine.timeout
import engine.entity.Entity
import engine.systems.System
import engine.systems.System.UpdateEntities
import engine.components.io.InputComponent

object PhysicsSystem {
  import engine.components.physics.Shape
  import engine.components.physics.Position

  def props( gx: Int, gy: Int ) = Props( classOf[ PhysicsSystem ], gx, gy )

  trait Data
  case class MobileData( e: Entity, p: Position, s: Shape, speed: Float, hops: Float ) extends Data
  case class StructData( e: Entity, p: Position, s: Shape )

  def getStructData( structs: Set[ Entity ] ): Set[ Future[ StructData ] ] =
    structs.map { e =>
      ( e( Dimension ) ? RequestSnapshot ).map {
        case s: DimensionComponent.Snapshot => StructData( e, s.pos, s.shape )
      }
    }

  def getMobileData( mobs: Set[ Entity ] ): Set[ Future[ MobileData ] ] =
    mobs.map( e => {
      val fDim = ( e( Dimension ) ? RequestSnapshot ).mapTo[ DimensionComponent.Snapshot ]
      val fMob = ( e( Mobility ) ? RequestSnapshot ).mapTo[ MobileComponent.Snapshot ]
      for ( dim <- fDim; mob <- fMob )
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
    for ( futureStruct <- addStructs )
      simulation.add( Await.result( futureStruct, 1000 millis ) )

    for ( futureMobile <- addMobiles ) {
      val data = Await.result( futureMobile, 1000 millis )
      simulation.createMobile( data )
    }

    for ( futureMobile <- remMobiles ) {
      val data = Await.result( futureMobile, 1000 millis )
      simulation.rem( data.e )
    }
  }

  override def receive = manage( 0, Set(), Set() )

  def manage( version: Long, structures: Set[ Entity ], mobiles: Set[ Entity ] ): Receive = LoggingReceive {
    case UpdateEntities( v, ents ) if v > version =>
      var newStructs: Set[ Entity ] = Set()
      var newMobiles: Set[ Entity ] = Set()

      for ( e <- ents ) {
        if ( e.hasComponents( mobileComponents ) ) newMobiles += e
        val comps = e.components
        if ( comps.contains( Dimension ) && !comps.contains( Mobility ) )
          newStructs += e
      }
      updateEntities( getStructData( newStructs -- structures ),
        getMobileData( newMobiles -- mobiles ),
        getMobileData( mobiles -- newMobiles ) )

      context.become( manage( v, newStructs, newMobiles ) )

    case Tick =>
      import InputComponent.Snapshot

      // Get inputs and apply them
      val futureSnaps: Set[ ( Entity, Future[ Snapshot ] ) ] =
        for ( e <- mobiles )
          yield ( e, ( e( Input ) ? RequestSnapshot ).mapTo[ Snapshot ] )

      for ( ( e, fs ) <- futureSnaps ) {
        simulation.applyInputs( e, Await.result( fs, 1000 millis ) )
      }

      simulation.step()

      // Update the components with new positions
      for ( ( e, b2Mob ) <- simulation.b2Mobiles ) {
        val x = b2Mob.body.getPosition.x
        val y = b2Mob.body.getPosition.y
        e( Dimension ) ! DimensionComponent.UpdatePosition( x, y )
      }

      sender ! TickAck
  }
}