package game.components.io

import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.communications.commands.ClientCommand
import game.components.Component
import game.components.physics.PositionComponent
import game.entity.EntityId

object ObserverComponent {
  def props( connection: ActorRef ) = Props( classOf[ ObserverComponent ], connection );

  // received
  case class Update( val snaps: Set[ ( EntityId, PositionComponent.Snapshot ) ] )
}

class ObserverComponent( val connection: ActorRef ) extends Component {
  import ObserverComponent._

  // All the things that I think are in the room with me:
  var snapshots: Map[ EntityId, PositionComponent.Snapshot ] = Map()

  override def receive = LoggingReceive {
    case Update( snaps ) ⇒
      for ( ( id, snap ) ← snaps -- snapshots.toSet ) {
        snapshots += id -> snap
        connection ! ClientCommand.CreateRect( id.toString, snap.pos, snap.shape )
      }

      for ( ( id, snap ) ← snapshots.toSet -- snaps ) {
        snapshots -= id
        // connection ! ClientCommand.DestroyRect(id.toString)
      }
  }

  override def postStop = {
    connection ! PoisonPill
  }
}