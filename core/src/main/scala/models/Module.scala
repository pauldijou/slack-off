package io.slackoff.core
package models

import akka.actor.{ ActorRef, Props }

import io.slackoff.core.controllers.ModuleController

case class Module(
  name: String,
  description: String,
  controller: Option[ModuleController] = None,
  actorProps: Option[Props] = None,
  actor: Option[ActorRef] = None // Don't try to set this one
  )
