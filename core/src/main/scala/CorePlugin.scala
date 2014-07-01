package io.slackoff.core

class CorePlugin(application: play.api.Application) extends play.api.Plugin {
  override def onStart = {
    Api.registerModule(
      "core",
      "Core module",
      Some(io.slackoff.core.controllers.CoreController),
      None
    )
  }
}
