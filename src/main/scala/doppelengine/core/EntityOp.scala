package doppelengine.core

import doppelengine.entity._

trait EntityOp {
  val v: Long
  override val toString = s"EntityOp-$v"
}

case class CreateEntities(v: Long, props: Set[EntityConfig]) extends EntityOp

case class RemoveEntities(v: Long, es: Set[Entity]) extends EntityOp