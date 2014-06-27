package io.slackoff

import scala.collection.mutable.Buffer
import scala.concurrent.duration._

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import io.slackoff.core.models.Module

package object core {
  private var modules: Buffer[Module] = Buffer.empty

  implicit val timeout = Timeout(5 seconds)

  def registerModule(
    name: String,
    description: String,
    actorProps: Option[Props] = None) = {

    val newModule = Module(
      name,
      description,
      actorProps,
      actor = actorProps map { io.slackoff.core.actors.Master.system.actorOf(_, name) }
    )

    modules += newModule

    newModule
  }

  def tellAll(message: Any) = modules foreach { _.actor map { _ ! message } }
  def !!(message: Any) = tellAll(message)

  def askAll(message: Any) = modules foreach { _.actor map { _ ? message } }
  def ??(message: Any) = askAll(message)
}
