name := "scalatree"

organization  := "com.doppelgamer"

version       := "0.1"

scalaVersion  := "2.10.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/",
  Opts.resolver.sonatypeSnapshots
)

libraryDependencies ++= Seq(
  "io.spray"          %  "spray-can"          % "1.1-M7",
  "io.spray"          %  "spray-routing"      % "1.1-M7",
  "io.spray"          %  "spray-testkit"      % "1.1-M7",
  "com.typesafe.akka" %% "akka-actor"         % "2.1.0",
  "org.scalatest"     %  "scalatest_2.10"     % "1.9.1" % "test",
  "com.typesafe"      %% "scalalogging-log4j" % "1.1.0-SNAPSHOT"
)

seq(Revolver.settings: _*)
