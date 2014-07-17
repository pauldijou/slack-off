import sbt._
import Keys._

object Dependencies {
  val playVersion = "2.3.1"
  val play = "com.typesafe.play" %% "play" % playVersion
  val playP = "com.typesafe.play" %% "play" % playVersion % "provided"
  val jsonP = "com.typesafe.play" %% "play-json" % playVersion % "provided"

  val akkaVersion = "2.3.3"
  val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaP = "com.typesafe.akka" %% "akka-actor" % akkaVersion % "provided"

  val jodaVersion = "2.3"
  val joda = "joda-time" % "joda-time" % jodaVersion
  val jodaP = "joda-time" % "joda-time" % jodaVersion % "provided"

  val scalaTestPlus = "org.scalatestplus" %% "play" % "1.1.0" % "test"
}
