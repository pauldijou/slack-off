package io.slackoff.core

import scala.collection.mutable.Buffer
import scala.concurrent.duration._

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import play.api.libs.concurrent.Akka
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WS
import play.api.Play.current

import io.slackoff.core.models._
import io.slackoff.core.utils._
import io.slackoff.core.controllers.ModuleController

object Api extends Log with Config {
  lazy val logger = initLogger("slackoff.core.Api")

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
      actorProps map { Akka.system.actorOf(_, name) }
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

  def send(hook: IncomingWebHook, team: Team, token: Option[String] = None) = {
    val teamName: String = team.name.toLowerCase
    val incomingToken: String = token orElse team.tokens.get("incoming").flatMap(_.headOption) getOrElse ""
    val url = s"https://${teamName}.slack.com/services/hooks/incoming-webhook?token=${incomingToken}"
    val jsonWebhook = Json.toJson(hook)

    debugStart(s"IncomingWebhooks.send at ${url}")
    debug(Json.prettyPrint(jsonWebhook))
    debugEnd

    WS.url(url).post(Json.stringify(jsonWebhook))
  }

  object teams {
    def all: Seq[Team] = core.teams
    def default: Team = core.teams(0)
    def byIdOption(id: String): Option[Team] = core.teams.find( _.id == id )
    def byNameOption(name: String): Option[Team] = core.teams.find( _.name == name )
    def byId(id: String): Team = teams.byIdOption(id) getOrElse teams.default
    def byName(name: String): Team = teams.byNameOption(name) getOrElse teams.default
    def from(id: String, name: Option[String] = None): Team = teams.byIdOption(id) orElse name.flatMap(teams.byNameOption) getOrElse teams.default
  }
}
