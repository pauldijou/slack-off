package io.slackoff.core
package actors

import akka.actor.{ ActorSystem, Props }

object Master {
  val system = ActorSystem("slackoff")

  val inner = system.actorOf(Props[Inner], name = "inner")
  val outer = system.actorOf(Props[Outer], name = "outer")

  def all(msg: Any) = ???

  // def all(msg: Any) = io.slackoff.core.modules foreach {
  //   _.actor map { _ ! msg }
  // }
}
