package io.slackoff

import scala.collection.mutable.Buffer
import akka.actor.Props

import io.slackoff.core.models.Module

package object core {
  protected var modules: Buffer[Module] = Buffer.empty

  def registerModule(
    name: String,
    description: String,
    actorProps: Option[Props] = None) = {
    modules += Module(
      name,
      description,
      actorProps,
      actor = actorProps map { io.slackoff.core.actors.Master.system.actorOf(_, name + "/" + "main") }
    )
  }
}
