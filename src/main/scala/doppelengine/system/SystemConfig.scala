package engine.system

import akka.actor.Props

case class SystemConfig(props: Props, name: String)
