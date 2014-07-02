package io.slackoff.core
package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

import io.slackoff.core.Api

case class OutgoingWebHook(
  token: String,
  team_id: String,
  team_domain: String,
  channel_id: String,
  channel_name: String,
  timestamp: String,
  user_id: String,
  user_name: String,
  text: Option[String],
  service_id: Option[String],
  trigger_word: Option[String]
) {
  lazy val content: String = this.text.getOrElse("")
  lazy val acceptable: Boolean = (this.user_id != "USLACKBOT") && this.text.isDefined
  lazy val team: Team = Api.teams.from(this.team_id, Option(this.team_domain))
  lazy val command: Option[Command] = this.trigger_word.flatMap { _ match {
    case "!" => {
      val words = content.split(" ")
      Option(Command(token, team_id, channel_id, channel_name, user_id, user_name, "/" + words.head, words.drop(1).mkString(" ")))
    }
    case _ => None
  }}
}

object OutgoingWebHook {
  implicit val outgoingWebHookFormat = Json.format[OutgoingWebHook]
}
