package doppelengine.core.operations

import doppelengine.entity.Entity

trait OperationResponse {
  val v: Long
}

trait Ack extends OperationResponse

trait Failure extends OperationResponse

case class EntityOpSuccess(v: Long) extends Ack

case class EntityOpFailure(v: Long, ents: Set[Entity]) extends Failure

case class SystemsOpAck(v: Long) extends Ack

case class SystemsOpFailure(v: Long) extends Failure