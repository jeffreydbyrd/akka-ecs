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
import game.core.Engine.timeout
import game.entity.Entity
import game.systems.System
import game.systems.System.UpdateEntities
import game.components.physics.MobileComponent
import akka.event.LoggingReceive
import game.components.io.InputComponent
import scala.concurrent.Await
import scala.concurrent.duration._
import org.jbox2d.dynamics.Body

object PhysicsSystem {
  import game.components.physics.Shape
  import game.components.physics.Position

  def props( gx: Int, gy: Int ) = Props( classOf[ PhysicsSystem ], gx, gy )

  trait Data
  case class MobileData( e: Entity, p: Position, s: Shape, speed: Float, hops: Float ) extends Data
  case class StructData( e: Entity, p: Position, s: Shape )
  sealed case class ApplyInputs( e: Entity, snapshot: InputComponent.Snapshot )

  def getStructData( structs: Set[ Entity ] ): Future[ Set[ StructData ] ] = {
    val setOfFutures =
      structs.map { e ⇒
        ( e( Dimension ) ? RequestSnapshot ).map {
          case s: DimensionComponent.Snapshot ⇒ StructData( e, s.pos, s.shape )
        }
      }
    Future.sequence( setOfFutures )
  }

  def getMobileData( mobs: Set[ Entity ] ): Future[ Set[ MobileData ] ] = {
    val setOfFutures =
      mobs.map( e ⇒ {
        val fDim = ( e( Dimension ) ? RequestSnapshot ).mapTo[ DimensionComponent.Snapshot ]
        val fMob = ( e( Mobility ) ? RequestSnapshot ).mapTo[ MobileComponent.Snapshot ]
        for ( dim ← fDim; mob ← fMob )
          yield MobileData( e, dim.pos, dim.shape, mob.speed, mob.hops )
      } )
    Future.sequence( setOfFutures )
  }
}

class PhysicsSystem( gx: Int, gy: Int ) extends System {
  import PhysicsSystem._

  val mobileComponents = List( Input, Dimension, Mobility )

  val simulation = new Box2dSimulation( gx, gy )
  var b2Mobiles: Map[ Entity, Box2dMobile ] = Map()

  override def receive = manage( 0, Set(), Set() )
  def manage( version: Long, structures: Set[ Entity ], mobiles: Set[ Entity ] ): Receive =
    LoggingReceive {
      case UpdateEntities( v, ents ) if v > version ⇒
        var newStructs: Set[ Entity ] = Set()
        var newMobiles: Set[ Entity ] = Set()

        for ( e ← ents ) {
          if ( e.hasComponents( mobileComponents ) ) newMobiles += e
          val comps = e.components
          if ( comps.contains( Dimension ) && !comps.contains( Mobility ) )
            newStructs += e
        }

        val addStructs: Set[ StructData ] =
          Await.result( getStructData( newStructs -- structures ), 500 millis )
        val addMobiles: Set[ MobileData ] =
          Await.result( getMobileData( newMobiles -- mobiles ), 500 millis )

        for ( sd ← addStructs ) simulation.add( sd )
        for ( md ← addMobiles ) b2Mobiles += md.e -> simulation.add( md )

        context.become( manage( v, newStructs, newMobiles ) )

      case ApplyInputs( e, snapshot ) ⇒
        for ( b2Mobile ← b2Mobiles.get( e ) ) {
          if ( !( snapshot.left ^ snapshot.right ) ) b2Mobile.setSpeed( 0 )
          else if ( snapshot.left ) b2Mobile.setSpeed( -b2Mobile.speed )
          else if ( snapshot.right ) b2Mobile.setSpeed( b2Mobile.speed )
        }

      case Tick ⇒
        for {
          e ← mobiles
          snap ← ( e( Input ) ? RequestSnapshot ).mapTo[ InputComponent.Snapshot ]
        } {
          self ! ApplyInputs( e, snap )
        }

        simulation.step()
        for ( ( e, b2Mob ) ← b2Mobiles ) {
          val x = b2Mob.body.getPosition.x
          val y = b2Mob.body.getPosition.y
          e( Dimension ) ! DimensionComponent.UpdatePosition( x, y )
        }
    }
}