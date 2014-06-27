package io.slackoff.core
package actors

import akka.actor.{ ActorSystem, Props }

object Master {
  val system = ActorSystem("slackoff")

  val inner = system.actorOf(Props[Inner], name = "inner")
  val outer = system.actorOf(Props[Outer], name = "outer")

  def all(msg: Any) = io.slackoff.core.tellAll(msg)
  def tellAll(msg: Any) = io.slackoff.core.tellAll(msg)
  def askAll(msg: Any) = io.slackoff.core.askAll(msg)
}
