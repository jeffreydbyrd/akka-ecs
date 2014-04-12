import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "doppelengine"
  val appVersion = "0.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.2",
    "com.typesafe.akka" %% "akka-agent" % "2.3.2",
    "com.typesafe.akka" %% "akka-testkit" % "2.3.2",
    "com.chuusai" % "shapeless_2.10.4" % "2.0.0",
    "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
  )

  val main = play.Project( appName, appVersion, appDependencies ).settings( 
    // Add your own project settings here      
  )

}
