package doppelengine.entity

import doppelengine.component.{ComponentType, ComponentConfig}

case class EntityConfig(val id: EntityId,
                        val components: Map[ComponentType, ComponentConfig])
