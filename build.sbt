name := """doppelengine"""

version := "0.0-SNAPSHOT"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.2.3"
  Seq(
    "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-contrib" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV
  )
}