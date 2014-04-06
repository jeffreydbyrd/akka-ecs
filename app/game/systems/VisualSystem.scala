package game.systems

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import akka.pattern.{ ask, pipe }
import game.components.Component
import game.components.ComponentType.Observer
import game.components.ComponentType.Physical
import game.components.physics.PhysicalComponent.Snapshot
import game.core.Game.Tick
import game.core.Game.timeout
import game.entity.EntityId
import game.components.ComponentType
import game.components.physics.PhysicalComponent
import game.core.Game
import scala.concurrent.Future
import game.components.io.ObserverComponent

object VisualSystem {
  def props = Props( classOf[ VisualSystem ] )
}

class VisualSystem extends Actor {
  import ComponentType.Physical
  import ComponentType.Observer
  import PhysicalComponent.Snapshot
  import Game.Tick
  import Game.timeout

  protected case class OutputNode( val id: EntityId, val dimensions: ActorRef )
  var clients: Set[ ActorRef ] = Set()
  var visuals: Set[ OutputNode ] = Set()

  override def receive = LoggingReceive {
    case System.UpdateComponents( ents ) ⇒
      var newClients: Set[ ActorRef ] = Set()
      var newVisuals: Set[ OutputNode ] = Set()
      for ( e ← ents ) {
        e.components.get( Physical ) foreach { newVisuals += OutputNode( e.id, _ ) }
        e.components.get( Observer ) foreach { newClients += _ }
      }
      clients = newClients
      visuals = newVisuals

    case Tick ⇒ // Send current Snapshot of the room to each client
      val setOfFutures: Set[ Future[ ( EntityId, Snapshot ) ] ] =
        visuals.map( v ⇒ ( v.dimensions ? Component.RequestSnapshot ).map {
          case snap: Snapshot ⇒ ( v.id, snap )
        } )

      val futureSet: Future[ ObserverComponent.Update ] =
        Future.sequence( setOfFutures ).map { ObserverComponent.Update( _ ) }

      for ( c ← clients ) futureSet.pipeTo( c )
  }
}