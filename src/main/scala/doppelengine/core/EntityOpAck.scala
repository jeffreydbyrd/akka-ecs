package doppelengine.core

import doppelengine.entity.Entity

trait EntityOpAck {
  val v: Long
}

case class EntityOpSuccess(v: Long) extends EntityOpAck

case class EntityOpFailure(v: Long, ents: Set[Entity]) extends EntityOpAck