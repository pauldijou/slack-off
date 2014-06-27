package io.slackoff.core
package actors

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WS
import play.api.Play.current

import io.slackoff.core.models.IncomingWebHook

object Hooker extends io.slackoff.core.utils.Config with io.slackoff.core.utils.Log {
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
}
