package game.systems

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.pattern.pipe
import game.components.Component
import game.components.ComponentType.Dimension
import game.components.ComponentType.Observer
import game.components.io.ObserverComponent
import game.components.physics.DimensionComponent.Snapshot
import game.core.Engine.Tick
import game.core.Engine.TickAck
import game.core.Engine.timeout
import game.entity.Entity
import game.entity.EntityId

object VisualSystem {
  def props = Props( classOf[ VisualSystem ] )
}

class VisualSystem extends Actor {
  override def receive = manage( 0, Set(), Set() )

  def manage( version: Long, clients: Set[ Entity ], visuals: Set[ Entity ] ): Receive =
    LoggingReceive {
      case System.UpdateEntities( v, ents ) if v > version ⇒
        var newClients: Set[ Entity ] = Set()
        var newVisuals: Set[ Entity ] = Set()
        for ( e ← ents ) {
          if ( e.components.contains( Dimension ) ) newVisuals += e
          if ( e.components.contains( Observer ) ) newClients += e
        }
        context.become( manage( v, newClients, newVisuals ) )

      case Tick ⇒ // Send current Snapshot of the room to each client
        val setOfFutures: Set[ Future[ ( EntityId, Snapshot ) ] ] =
          visuals.map( v ⇒ ( v( Dimension ) ? Component.RequestSnapshot ).map {
            case snap: Snapshot ⇒ ( v.id, snap )
          } )

        // that's some sexy code
        val futureSet: Future[ ObserverComponent.Update ] =
          Future.sequence( setOfFutures ).map { ObserverComponent.Update( _ ) }

        for ( c ← clients ) futureSet.pipeTo( c( Observer ) )
        
        sender ! TickAck
    }
}