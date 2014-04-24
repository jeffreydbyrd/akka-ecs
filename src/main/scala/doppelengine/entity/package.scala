package engine

import engine.component.{ComponentType, ComponentConfig}

package object entity {
  type EntityConfig = Map[ComponentType, ComponentConfig]
}
