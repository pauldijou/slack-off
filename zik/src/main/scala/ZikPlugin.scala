package io.slackoff.zik

import akka.actor.Props

import io.slackoff.core.Api

class JiraPlugin(application: play.api.Application) extends play.api.Plugin {
  override def onStart = {
    Api.registerModule(
      "zik",
      "Seach, listen, enjoy... anywhere, anytime",
      Some(ZikController),
      Some(Props[Zaktor])
    )
  }
}
