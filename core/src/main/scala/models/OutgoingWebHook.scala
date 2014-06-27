package io.slackoff.core
package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

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
  lazy val content = this.text.getOrElse("")
  lazy val acceptable = (this.user_id != "USLACKBOT") && this.text.isDefined
}
