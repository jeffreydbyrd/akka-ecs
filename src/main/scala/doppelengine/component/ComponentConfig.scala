package doppelengine.component

import akka.actor.Props

case class ComponentConfig(val p: Props, val id: String)
