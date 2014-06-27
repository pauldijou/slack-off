package io.slackoff.core
package models

import akka.actor.{ ActorRef, Props }

case class Module(
  name: String,
  description: String,
  actorProps: Option[Props] = None,
  actor: Option[ActorRef] = None // Don't try to set this one
  )
