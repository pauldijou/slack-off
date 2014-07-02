package io.slackoff.jira

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import akka.actor.Actor

import io.slackoff.core.models.{ Command, OutgoingWebHook, IncomingWebHook }

class Jiractor extends Actor with io.slackoff.core.utils.Answer with JiraConfig {
  def receive = {
    case c: Command         ⇒ handleCommand(c)
    case h: OutgoingWebHook ⇒ handleOutgoingWebHook(h)
    case _                  ⇒ ()
  }

  def handleCommand(command: Command) = {
    command.args.headOption match {
      case Some("") ⇒ asyncError("/jira command must have at least one argument.")
      case Some("new") ⇒ {
        JiraServices.create(command.args(2), command.args(3), command.channel_name.toUpperCase, command.args(1)).map { r ⇒
          if (r.keys.contains("key")) {
            val key = (r \ "key").toString
            val link = JiraServices.issueUrl(key)
            ok(s"<@${command.user_name}> juste created JIRA <${link}|${key}>")
          } else {
            error(r.toString)
          }
        }
      }
      case Some(key) ⇒ {
        val link = JiraServices.issueUrl(key)
        asyncOk(s"<${link}|${key}>")
      }
      case _ ⇒ asyncError("/jira command must have at least one argument.")
    }
  }

  val jiraRegex = "([a-zA-Z]+-[0-9]+)".r

  def handleOutgoingWebHook(hook: OutgoingWebHook) = {
    Future.sequence((for {
      jiraRegex(issueKey) ← jiraRegex findAllIn hook.content
    } yield issueKey).toList map {
      key ⇒ JiraServices.get(key).map { (key, _) }
    }).map { issues ⇒
      ("JIRA",
        issues.map {
          case (key, None) ⇒ s"${key}: No issue found, sorry."
          case (key, Some(issue)) ⇒ {
            val link = JiraServices.issueUrl(issue.key)
            s"<${link}|${issue.key}> [${issue.fields.priority.name}]: ${issue.fields.summary} (by ${issue.fields.creator.displayName})"
          }
        }.mkString("\n"))
    }
  }
}
