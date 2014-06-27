package io.slackoff.core
package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class IncomingWebHookAttachmentField(title: String, value: String, short: Boolean = false)

object IncomingWebHookAttachmentField {
  implicit val incomingWebHookAttachmentFieldFormat = Json.format[IncomingWebHookAttachmentField]
}

case class IncomingWebHookAttachment(
  fallback: String,
  text: Option[String] = None,
  pretext: Option[String] = None,
  color: Option[String] = None, // Hex code or 'good' or 'warning' or 'danger'
  fields: List[IncomingWebHookAttachmentField] = List()
)

object IncomingWebHookAttachment {
  implicit val incomingWebHookAttachmentFormat = Json.format[IncomingWebHookAttachment]
}

case class IncomingWebHook(
  text: String,
  username: Option[String] = None,
  channel: Option[String] = None,
  icon_url: Option[String] = None,
  icon_emoji: Option[String] = None,
  attachments: Option[List[IncomingWebHookAttachment]] = None
) {
  def send = io.slackoff.core.actors.Hooker.send(this)
}

object IncomingWebHook {
  implicit val incomingWebHookFormat = Json.format[IncomingWebHook]
}
