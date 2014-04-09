package game.components.io

import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.communications.commands.ClientCommand
import game.components.Component
import game.components.physics.DimensionComponent
import game.entity.EntityId

object ObserverComponent {
  def props( connection: ActorRef ) = Props( classOf[ ObserverComponent ], connection );

  // received
  case class Update( val snaps: Set[ ( EntityId, DimensionComponent.Snapshot ) ] )
}

class ObserverComponent( val connection: ActorRef ) extends Component {
  import ObserverComponent._

  // All the things that I think are in the room with me:
  var snapshots: Map[ EntityId, DimensionComponent.Snapshot ] = Map()

  override def receive = LoggingReceive {
    case up @ Update( snaps ) ⇒
      for ( ( id, snap ) ← snaps if !snapshots.contains( id ) )
        connection ! ClientCommand.CreateRect( id.toString, snap.pos, snap.shape )

      val movements =
        for {
          ( id, snap ) ← snaps
          if snapshots.contains( id ) && snap.pos != snapshots( id ).pos
        } yield id.toString -> ( snap.pos.x, snap.pos.y )

      if ( movements.nonEmpty )
        connection ! ClientCommand.UpdatePositions( movements.toMap )

      snapshots = snaps.toMap
  }

  override def postStop = {
    connection ! PoisonPill
  }
}