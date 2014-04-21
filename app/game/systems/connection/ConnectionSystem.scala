package game.systems.connection


import akka.actor._
import akka.event.LoggingReceive
import engine.system.System
import play.api.libs.iteratee.Enumerator
import game.components.io.{ObserverComponent, InputComponent}
import game.components.physics.{MobileComponent, DimensionComponent}
import engine.component.{ComponentConfig, ComponentType}
import engine.core.Engine
import engine.system.System.UpdateEntities
import akka.actor.Terminated
import engine.communications.connection.PlayActorConnection
import engine.entity.EntityConfig

object ConnectionSystem {
  def props = Props(classOf[ConnectionSystem])

  // received
  case class AddPlayer(name: String)

  // sent
  case object Connect

  case class Connected(connection: ActorRef, enum: Enumerator[String])

  case class NotConnected(message: String)

}

class ConnectionSystem extends System {

  import ConnectionSystem._
  import ComponentType._
  import Engine._

  var connections: Map[String, ActorRef] = Map()
  var numConnections: Int = 0
  var entityVersion: Long = 0

  def connectPlayer(username: String) = {
    val (enumerator, channel) = play.api.libs.iteratee.Concurrent.broadcast[String]

    val connection =
      context.actorOf(PlayActorConnection.props(channel), s"conn$numConnections")

    val input =
      new ComponentConfig(InputComponent.props, s"input_plr$numConnections")
    val observer =
      new ComponentConfig(ObserverComponent.props, s"observer_plr$numConnections")
    val dimensions =
      new ComponentConfig(DimensionComponent.props(10, 10, 2, 2), s"dimensions_plr$numConnections")
    val mobility =
      new ComponentConfig(MobileComponent.props(5, 8F), s"mobile_plr$numConnections")

    val configs: EntityConfig = Map(
      Input -> input, Observer -> observer,
      Dimension -> dimensions, Mobility -> mobility
    )

    context.actorOf(
      Helper.props(context.parent, connection, numConnections, entityVersion, configs),
      s"helper$numConnections")

    sender ! Connected(connection, enumerator)
    numConnections += 1
    connections += username -> connection
    context.watch(connection)
  }


  override def receive: Receive = LoggingReceive {
    case UpdateEntities(v, _) => entityVersion = v
    case Tick => sender ! TickAck

    case AddPlayer(username) if !connections.contains(username) =>
      connectPlayer(username)

    case AddPlayer(username) =>
      sender ! NotConnected(s"username '$username' already in use")

    case Terminated(conn) =>
      connections = connections.filterNot {
        case (usrName, actRef) => actRef == conn
      }
  }
}
