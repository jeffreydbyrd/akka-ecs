package game.core

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.components.physics.DimensionComponent
import game.entity.Entity
import game.entity.StructureEntity
import game.systems.QuitSystem
import game.systems.System
import game.systems.VisualSystem
import game.systems.physics.PhysicsSystem
import game.util.logging.AkkaLoggingService
import game.components.io.InputComponent
import game.communications.connection.PlayActorConnection
import game.components.io.ObserverComponent
import game.components.physics.MobileComponent
import play.api.libs.iteratee.Enumerator
import game.entity.PlayerEntity
import akka.actor.ActorSystem
import akka.actor.Terminated

object Engine {
  implicit val timeout: akka.util.Timeout = 1 second
  val system: ActorSystem = akka.actor.ActorSystem( "Doppelsystem" )
  val props = Props( classOf[ Engine ] )
  val engine: ActorRef = system.actorOf( props, name = "engine" )

  // Received:
  case class AddPlayer( name: String )
  trait EntityOp {
    val v: Long
    val es: Set[ Entity ]
  }
  case class Add( val v: Long, val es: Set[ Entity ] ) extends EntityOp
  case class Rem( val v: Long, val es: Set[ Entity ] ) extends EntityOp
  case object Tick

  // sent:
  case object Connect
  case class Connected( connection: ActorRef, enum: Enumerator[ String ] )
  case class NotConnected( message: String )
}

class Engine extends Actor {
  import Engine._

  val logger = new AkkaLoggingService( this, context )
  val ticker =
    system.scheduler.schedule( 20 milliseconds, 20 milliseconds, self, Tick )

  private var systems: Set[ ActorRef ] = Set(
    context.actorOf( QuitSystem.props( self ), "quit_system" ),
    context.actorOf( VisualSystem.props, "visual_system" ),
    context.actorOf( PhysicsSystem.props( 0, -10 ), "physics_system" )
  )

  def updateEntities( v: Long, ents: Set[ Entity ] ) = {
    logger.info( s"Entities v-$v: $ents" )
    for ( sys ← systems ) {
      sys ! System.UpdateEntities( v, ents )
      context.become( manage( v, ents ) )
    }
  }

  var connections: Map[ String, ActorRef ] = Map()

  def connectPlayer( username: String, version: Long ) = {
    val ( enumerator, channel ) = play.api.libs.iteratee.Concurrent.broadcast[ String ]
    val input = context.actorOf( InputComponent.props, s"input$version" )
    val connection =
      context.actorOf( PlayActorConnection.props( input, channel ), s"conn$version" )
    val output =
      context.actorOf( ObserverComponent.props( connection ), s"observer$version" )
    val dimensions =
      context.actorOf( DimensionComponent.props( 10, 10, 1, 2 ), s"dimensions$version" )
    val velocity =
      context.actorOf( MobileComponent.props( 5, 0 ), s"mobile$version" )
    sender ! Connected( connection, enumerator )
    connections += username -> connection
    context.watch( connection )
    new PlayerEntity( input, output, dimensions, velocity )
  }

  var walls: Set[ Entity ] = Set(
    new StructureEntity( context.actorOf( DimensionComponent.props( 25, 1, 50, 1 ), "floor" ) )
  )

  override def receive = manage( 0, walls )

  def manage( version: Long, entities: Set[ Entity ] ): Receive = LoggingReceive {
    case Tick                 ⇒ for ( sys ← systems ) sys ! Tick

    case Add( `version`, es ) ⇒ updateEntities( version + 1, entities ++ es )

    case Rem( `version`, es ) ⇒
      updateEntities( version + 1, entities -- es )
      for ( e ← es; ( _, comp ) ← e.components ) comp ! PoisonPill

    case op: EntityOp if op.v < version ⇒
      sender ! System.UpdateEntities( version, entities )

    case AddPlayer( username ) if !connections.contains( username ) ⇒
      val v = version + 1
      updateEntities( v, entities + connectPlayer( username, v ) )

    case AddPlayer( username ) ⇒
      sender ! NotConnected( s"username '$username' already in use" )

    case Terminated( conn ) ⇒
      connections = connections.filterNot { case ( usrName, actRef ) ⇒ actRef == conn }
  }

}