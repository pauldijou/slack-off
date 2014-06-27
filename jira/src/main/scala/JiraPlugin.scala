package io.slackoff.jira

import akka.actor.Props

import io.slackoff.core._

class JiraPlugin(application: play.api.Application) extends play.api.Plugin {
  override def onStart = {
    registerModule(
      "jira",
      "JIRA",
      Some(Props[Jiractor])
    )
  }
}