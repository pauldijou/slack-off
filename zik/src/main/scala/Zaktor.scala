package io.slackoff.zik

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import akka.actor.Actor

import io.slackoff.core.models.{ Command, OutgoingWebHook, IncomingWebHook }

class Zaktor extends Actor with ZikConfig with io.slackoff.core.utils.Log {

  lazy val logger = initLogger("slackoff.modules.zik.zaktor")

  def receive = {
    case c: Command         ⇒ handleCommand(c)
    case _                  ⇒ ()
  }

  def handleCommand(command: Command) = {
    debug(s"Handling command ${command}...")
    val channel = Option(command.channel_id)
    command.command match {
      case "/np" ⇒ {
        IncomingWebHook("Should display playlist...", channel = channel).send(command.team)
      }
      case "/search" => {
        ZikServices.soundcloud.tracks.search(command.text.trim) map { ziks =>
          IncomingWebHook(ziks map { _.title } mkString "\n", channel = channel).send(command.team)
        }
      }
      case _ ⇒ ()
    }
  }
}
