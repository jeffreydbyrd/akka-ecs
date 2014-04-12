package game

import game.util.GenericHMap
import akka.agent.Agent

package object components {
  type ComponentMap = GenericHMap[ CompType, Agent ]
}