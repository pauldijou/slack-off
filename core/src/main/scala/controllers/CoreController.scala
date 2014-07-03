package io.slackoff.core
package controllers

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.data._
import play.api.data.Forms._

import io.slackoff.core.Api
import io.slackoff.core.models._
import  io.slackoff.core.utils._

object CoreController extends ModuleController with Answer with Config {

  lazy val logger = initLogger("slackoff.modules.core.controllers.core")

  def hasRoute(rh: RequestHeader) = true

  def applyRoute[RH <: RequestHeader, H >: Handler](rh: RH, default: RH ⇒ H) =
    (rh.method, rh.path.drop(path.length)) match {
      case ("POST", "/outgoings") ⇒ handleOutgoing
      case ("POST", "/commands")  ⇒ handleCommand
      case _                      ⇒ default(rh)
    }

  // OUTGOING WEBHOOK
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

  def handleOutgoing = Action { implicit request ⇒
    debugStart("core.controllers.core.handleOutgoing")
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
        debugEnd
        if (!hook.acceptable) { Ok }
        else {
          Api !! hook
          hook.command.map(broadcastCommand)
          Ok
        }
      }
    )
  }

  // COMMANDS
  val token = "FGbEeBew4N8NiCwcBcK9Qp3e"

  val commandForm = Form(
    mapping(
      "token" -> nonEmptyText,
      "team_id" -> nonEmptyText,
      "channel_id" -> nonEmptyText,
      "channel_name" -> nonEmptyText,
      "user_id" -> nonEmptyText,
      "user_name" -> nonEmptyText,
      "command" -> nonEmptyText,
      "text" -> nonEmptyText
    )(Command.apply)(Command.unapply)
  )

  def broadcastCommand(command: Command): Future[SimpleResult] = command.command match {
    case "/do" ⇒ {
      command.args.headOption match {
        case None ⇒ asyncError("/do command must have at least one argument.")
        case Some(newCommand) ⇒ {
          val newText = command.args.drop(1).mkString(" ")
          broadcastCommand(command.copy(command = "/" + newCommand, text = newText))
        }
      }
    }
    case _ ⇒ {
      debug(s"Broadcasting command ${command}...")
      Api !! command
      asyncOk
    }
  }

  def handleCommand = Action.async { implicit request ⇒
    commandForm.bindFromRequest.fold(
      errors ⇒ asyncError("Wrong command submission from Slack."),
      command ⇒ {
        if (command.token != token) asyncError("wrong token.")
        else broadcastCommand(command)
      }
    )
  }
}
