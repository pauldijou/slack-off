package io.slackoff.core
package actors

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

import io.slackoff.core.models.OutgoingWebHook

class Outer extends Actor with io.slackoff.core.utils.Config {
  def receive = {
    case h: OutgoingWebHook ⇒ context.actorSelection("../*") ! h
    case _                  ⇒ ()
  }
}
