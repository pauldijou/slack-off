package io.slackoff.core

import scala.collection.mutable.Buffer
import scala.concurrent.duration._

import akka.actor.{ ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WS
import play.api.Play.current

import io.slackoff.core.models.{ Module, IncomingWebHook }
import io.slackoff.core.utils.{ Log, Config }
import io.slackoff.core.controllers.ModuleController

object Api extends Log with Config {
  lazy val logger = play.api.Logger("core.Api")

  private val system = ActorSystem("slackoff")
  private var modules: Buffer[Module] = Buffer.empty

  implicit val timeout = Timeout(5 seconds)

  def registerModule(
    name: String,
    description: String,
    controller: Option[ModuleController] = None,
    actorProps: Option[Props] = None
  ) = {
    val newModule = Module(
      name,
      description,
      controller,
      actorProps,
      actorProps map { system.actorOf(_, name) }
    )

    modules += newModule

    newModule
  }

  def getModules: Seq[Module] = modules.toSeq

  def getModule(name: String): Option[Module] = modules.find { _.name == name }

  def tellAll(message: Any) = modules foreach { _.actor map { _ ! message } }
  def !!(message: Any) = tellAll(message)

  def askAll(message: Any) = modules foreach { _.actor map { _ ? message } }
  def ??(message: Any) = askAll(message)

  private lazy val url =
    s"https://${slack.team.name}.slack.com/services/hooks/incoming-webhook?token=${slack.hooks.incoming.token}"

  def send(hook: IncomingWebHook) = {
    val jsWebhook = Json.toJson(hook)

    debugStart("IncomingWebhooks.send")
    debug(Json.prettyPrint(jsWebhook))
    debugEnd

    WS.url(url).post(Json.stringify(jsWebhook))
  }
}
