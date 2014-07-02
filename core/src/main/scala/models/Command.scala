package io.slackoff.core
package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

import io.slackoff.core.Api

case class Command(
  token: String,
  team_id: String,
  channel_id: String,
  channel_name: String,
  user_id: String,
  user_name: String,
  command: String,
  text: String
) {
  lazy val name: String = command.drop(1)
  lazy val args: List[String] = text.split(" ").toList
  lazy val team: Team = Api.teams.from(this.team_id)
}
