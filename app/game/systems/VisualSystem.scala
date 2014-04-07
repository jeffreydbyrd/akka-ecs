package game.systems

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import akka.actor.Actor
import akka.actor.Props
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.pattern.pipe
import game.components.Component
import game.components.ComponentType.Observer
import game.components.ComponentType.Position
import game.components.io.ObserverComponent
import game.components.physics.PositionComponent.Snapshot
import game.core.Engine.Tick
import game.core.Game.timeout
import game.entity.Entity
import game.entity.EntityId

object VisualSystem {
  def props = Props( classOf[ VisualSystem ] )
}

class VisualSystem extends Actor {

  var version: Long = 0
  var clients: Set[ Entity ] = Set()
  var visuals: Set[ Entity ] = Set()

  override def receive = LoggingReceive {
    case System.UpdateEntities( v, ents ) if v > version ⇒
      version = v
      var newClients: Set[ Entity ] = Set()
      var newVisuals: Set[ Entity ] = Set()
      for ( e ← ents ) {
        if ( e.components.contains( Position ) ) newVisuals += e
        if ( e.components.contains( Observer ) ) newClients += e
      }
      clients = newClients
      visuals = newVisuals

    case Tick ⇒ // Send current Snapshot of the room to each client
      val setOfFutures: Set[ Future[ ( EntityId, Snapshot ) ] ] =
        visuals.map( v ⇒ ( v( Position ) ? Component.RequestSnapshot ).map {
          case snap: Snapshot ⇒ ( v.id, snap )
        } )

      // that's some sexy code
      val futureSet: Future[ ObserverComponent.Update ] =
        Future.sequence( setOfFutures ).map { ObserverComponent.Update( _ ) }

      for ( c ← clients ) futureSet.pipeTo( c( Observer ) )
  }
}