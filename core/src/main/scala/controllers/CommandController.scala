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

import io.slackoff.core.models._

object CommandController
  extends io.slackoff.core.controllers.ModuleController
  with io.slackoff.core.utils.Answer
  with io.slackoff.core.utils.Config {

  lazy val logger = Logger("hooks.commands")

  def hasRoute(rh: RequestHeader) = true

  def applyRoute[RH <: RequestHeader, H >: Handler](rh: RH, default: RH => H) = 
    (rh.method, rh.path.drop(path.length)) match {
      case ("POST", "") => handle
      case _ => default(rh)
    }

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
    case "/do" => {
      command.args.headOption match {
        case None => asyncError("/do command must have at least one argument.")
        case Some(newCommand) => {
          val newText = command.args.drop(1).mkString(" ")
          broadcastCommand(command.copy(command = "/" + newCommand, text = newText))
        }
      }
    }
    // case "/jira" => handleJira(command)
    case _ => {
      io.slackoff.core.actors.Master.all(command)
      asyncOk
    }//asyncError("unknow command '" + command.command + "'.")
  }

  def handle = Action.async { implicit request =>
    commandForm.bindFromRequest.fold(
      errors => asyncError("wrong command submission from Slack."),
      command => {
        if (command.token != token) asyncError("wrong token.")
        else broadcastCommand(command)
      }
    )
  }
}
