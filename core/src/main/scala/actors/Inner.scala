package io.slackoff.core
package actors

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WS
import play.api.Play.current

import io.slackoff.core.models.IncomingWebHook

class Inner extends Actor with io.slackoff.core.utils.Log with io.slackoff.core.utils.Config {
  lazy val logger = play.api.Logger("hooks.incoming")
  lazy val url =
    s"https://${slack.team.name}.slack.com/services/hooks/incoming-webhook?token=${slack.hooks.incoming.token}"

  def send(hook: IncomingWebHook) = {
    val jsWebhook = Json.toJson(hook)

    debugStart("IncomingWebhooks.send")
    debug(Json.prettyPrint(jsWebhook))
    debugEnd

    WS.url(url).post(Json.stringify(jsWebhook))
  }

  def receive = {
    case h:IncomingWebHook => send(h)
    case _      => ()
  }
}
