import Dependencies._
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

val wsP = ws % "provided"

val commonSettings = Seq(
  organization := "io.slackoff",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.11.1",
  resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  resolvers += "Typesafe repository mwn" at "http://repo.typesafe.com/typesafe/maven-releases/"
)

lazy val core = Project("core", file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "slackoff-core",
    libraryDependencies ++= Seq(akka, jsonP, playP, wsP, scalaTestPlus)
  )
  .enablePlugins(SbtTwirl)

lazy val bitbucket = Project("bitbucket", file("bitbucket"))
  .settings(commonSettings: _*)
  .settings(
    name := "slackoff-bitbucket",
    libraryDependencies ++= Seq(jsonP, playP, wsP)
  )
  .dependsOn(core)

lazy val jira = Project("jira", file("jira"))
  .settings(commonSettings: _*)
  .settings(
    name := "slackoff-jira",
    libraryDependencies ++= Seq(jsonP, playP, wsP)
  )
  .dependsOn(core)

lazy val kudo = Project("kudo", file("kudo"))
  .settings(commonSettings: _*)
  .settings(
    name := "slackoff-kudo",
    libraryDependencies ++= Seq(jsonP, playP, wsP)
  )
  .dependsOn(core)

lazy val zik = Project("zik", file("zik"))
  .settings(commonSettings: _*)
  .settings(
    name := "slackoff-zik",
    libraryDependencies ++= Seq(jsonP, playP, wsP)
  )
  .dependsOn(core)

lazy val server = Project("server", file("server"))
  .settings(commonSettings: _*)
  .settings(
    name := "slackoff-server",
    libraryDependencies ++= Seq(joda, json, play, ws)
  )
  .enablePlugins(PlayScala)
  .enablePlugins(SbtTwirl)
  .dependsOn(bitbucket, jira, kudo, zik)

lazy val slackOff = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "slackoff"
  )
  .aggregate(core, bitbucket, jira, kudo, zik, server)
