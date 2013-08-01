import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "scalatree"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.typesafe.akka" %% "akka-testkit" % "2.3-SNAPSHOT"
  )

  val main = play.Project( appName, appVersion, appDependencies ).settings( 
    // Add your own project settings here      
  )

}
