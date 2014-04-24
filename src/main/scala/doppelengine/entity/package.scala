package doppelengine

import doppelengine.component.{ComponentType, ComponentConfig}

package object entity {
  type EntityConfig = Map[ComponentType, ComponentConfig]
}
