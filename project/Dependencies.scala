import sbt._
import Keys._

object Dependencies {
  val playVersion = "2.3.1"
  val akkaVersion = "2.3.3"

  val joda = "joda-time" % "joda-time" % "2.3"
  val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  // val json  = "com.typesafe.play" %% "play-json"         % playVersion
  val play = "com.typesafe.play" %% "play" % playVersion
  val jsonP = "com.typesafe.play" %% "play-json" % playVersion % "provided"
  val playP = "com.typesafe.play" %% "play" % playVersion % "provided"
}
