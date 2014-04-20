package engine.systems

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.pattern.pipe
import engine.components.Component
import engine.components.ComponentType.Dimension
import engine.components.ComponentType.Observer
import engine.components.io.ObserverComponent
import engine.components.physics.DimensionComponent.Snapshot
import engine.core.Engine.Tick
import engine.core.Engine.TickAck
import engine.core.Engine.timeout
import engine.entity.Entity
import engine.entity.EntityId

object VisualSystem {
  def props = Props( classOf[ VisualSystem ] )
}

class VisualSystem extends Actor {
  override def receive = manage( 0, Set(), Set() )

  def manage( version: Long, clients: Set[ Entity ], visuals: Set[ Entity ] ): Receive =
    LoggingReceive {
      case System.UpdateEntities( v, ents ) if v > version =>
        var newClients: Set[ Entity ] = Set()
        var newVisuals: Set[ Entity ] = Set()
        for ( e <- ents ) {
          if ( e.components.contains( Dimension ) ) newVisuals += e
          if ( e.components.contains( Observer ) ) newClients += e
        }
        context.become( manage( v, newClients, newVisuals ) )

      case Tick => // Send current Snapshot of the room to each client
        val setOfFutures: Set[ Future[ ( EntityId, Snapshot ) ] ] =
          visuals.map( v => ( v( Dimension ) ? Component.RequestSnapshot ).map {
            case snap: Snapshot => ( v.id, snap )
          } )

        // that's some sexy code
        val futureSet: Future[ ObserverComponent.Update ] =
          Future.sequence( setOfFutures ).map { ObserverComponent.Update( _ ) }

        for ( c <- clients ) futureSet.pipeTo( c( Observer ) )
        
        sender ! TickAck
    }
}