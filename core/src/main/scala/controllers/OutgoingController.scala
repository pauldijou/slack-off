package io.slackoff.core
package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.data.Form
import play.api.data.Forms._

import io.slackoff.core.models._

object OutgoingController
    extends io.slackoff.core.controllers.ModuleController
    with io.slackoff.core.utils.Config {

  lazy val logger = Logger("hooks.messages")

  def hasRoute(rh: RequestHeader) = true

  def applyRoute[RH <: RequestHeader, H >: Handler](rh: RH, default: RH ⇒ H) =
    (rh.method, rh.path.drop(path.length)) match {
      case ("POST", "") ⇒ handle
      case _            ⇒ default(rh)
    }

  val hookForm = Form(
    mapping(
      "token" -> nonEmptyText,
      "team_id" -> nonEmptyText,
      "team_domain" -> nonEmptyText,
      "channel_id" -> nonEmptyText,
      "channel_name" -> nonEmptyText,
      "timestamp" -> nonEmptyText,
      "user_id" -> nonEmptyText,
      "user_name" -> nonEmptyText,
      "text" -> optional(text),
      "service_id" -> optional(text),
      "trigger_word" -> optional(text)
    )(OutgoingWebHook.apply)(OutgoingWebHook.unapply)
  )

  def handle = Action { implicit request ⇒
    debugStart("OutgoingWebHooks.handle")
    hookForm.bindFromRequest.fold(
      errors ⇒ {
        debug("ERROR parsing: " + request.body)
        debugEnd
        BadRequest(Json.stringify(Json.obj(
          "text" -> ("Server-side error: " + errors.toString)
        )))
      },
      hook ⇒ {
        debug(hook.toString)
        println(hook.toString)
        debugEnd
        if (!hook.acceptable) { Ok }
        else {
          io.slackoff.core.actors.Master.outer ! hook
          Ok
        }
      }
    )
  }
}
