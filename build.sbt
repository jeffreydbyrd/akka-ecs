name := """doppelengine"""

version := "0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.jbox2d" % "jbox2d" % "2.2.1.1",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.2.1",
  "com.typesafe.akka" %% "akka-contrib" % "2.2.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.1",
  "org.webjars" %% "webjars-play" % "2.2.1"
)

play.Project.playScalaSettings
