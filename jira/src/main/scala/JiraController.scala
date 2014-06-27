package io.slackoff.jira

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import io.slackoff.core.models._
import io.slackoff.jira.JiraWebhookAction._

object JiraController
    extends io.slackoff.core.controllers.ModuleController
    with JiraConfig {

  lazy val logger = Logger("hooks.jira")

  def hasRoute(rh: RequestHeader) = true

  def applyRoute[RH <: RequestHeader, H >: Handler](rh: RH, default: RH ⇒ H) =
    (rh.method, rh.path.drop(path.length)) match {
      case ("GET", "")  ⇒ Action { Ok("JIRA plugin is currently running...") }
      case ("POST", "") ⇒ handleWebhook
      case _            ⇒ default(rh)
    }

  def handleWebhook = Action(parse.json) { implicit request ⇒

    request.body.validate[JiraWebhookEvent].fold(
      errors ⇒ {
        debug(Json.prettyPrint(request.body))
        debug(errors)
      },
      event ⇒ {
        debug(Json.prettyPrint(Json.toJson(event)))
        val action = event.webhookEvent
        val username = request.getQueryString("username") orElse jira.bot.name
        val channel = request.getQueryString("channel").map("#" + _)
        val iconUrl = request.getQueryString("iconUrl") orElse jira.bot.icon

        val issue = event.issue
        val fields = issue.fields

        val issueName = event.issue.key
        val issueLink = JiraServices.issueUrl(issueName)
        val issueType = fields.issuetype.name
        val updatedBy = event.user.displayName

        var message = ""
        var attachmentsBuffer = scala.collection.mutable.ListBuffer[IncomingWebHookAttachment]()

        val attachmentIssueSummary = IncomingWebHookAttachmentField("Summary", fields.summary)
        val defaultColor =
          if (event.created) { jira.colors.issue.created }
          else if (event.deleted) { jira.colors.issue.deleted }
          else jira.colors.issue.updated

        val defaultAttachment = IncomingWebHookAttachment(
          s"Created by ${fields.creator.displayName}. Summary: ${fields.summary}",
          None, None, defaultColor,
          List(attachmentIssueSummary)
        )

        if (event.created) {
          message = s"[${fields.priority.name}] ${issueType} <${issueLink}|${issueName}> has been created by ${updatedBy}."
          val improvedFields =
            defaultAttachment.fields :+
              IncomingWebHookAttachmentField("Description", fields.description.filterNot(_.isEmpty).getOrElse("(None)"))

          attachmentsBuffer += defaultAttachment.copy(fields = improvedFields)
        } else if (event.deleted) {
          message = s"[${fields.priority.name}] ${issueType} <${issueLink}|${issueName}> has been deleted by ${updatedBy}."
          attachmentsBuffer += defaultAttachment
        } else if (event.worklogUpdated && jira.worklog.enabled) {
          message = s"Worklog of <${issueLink}|${issueName}> has been updated by ${updatedBy}."
          attachmentsBuffer += defaultAttachment
        } else if (event.changedlog) {
          val changelog = event.changelog.get
          val validItems = changelog.items.filterNot { item ⇒
            jira.blackList.exists(e ⇒ e.toLowerCase == item.field.toLowerCase)
          }

          if (!validItems.isEmpty) {
            message = s"[${fields.priority.name}] ${issueType} <${issueLink}|${issueName}> has been updated by ${updatedBy} (${fields.summary})."
            validItems.foreach { item ⇒
              val from = item.fromStr.filterNot(_.isEmpty)
              val to = item.toStr.filterNot(_.isEmpty).getOrElse("(None)")
              val attachMsg = s"""[${item.field}] ${to} ${from.map("(was " + _ + ")").getOrElse("")}"""

              if (from.map(_.length > 25).getOrElse(false) || to.length > 25) {
                attachmentsBuffer += IncomingWebHookAttachment(
                  attachMsg, None, None, None,
                  List(
                    IncomingWebHookAttachmentField("Field", item.field),
                    IncomingWebHookAttachmentField("To", to),
                    IncomingWebHookAttachmentField("From", from.getOrElse("(None)"))
                  )
                )
              } else {
                attachmentsBuffer += IncomingWebHookAttachment(
                  attachMsg, Some(attachMsg), None, None, List()
                )
              }
            }
          }
        }

        if (event.commented) {
          val comment = event.comment.get
          var fieldName = "Content"

          (message.length > 1, event.newlyCommented) match {
            case (true, true)  ⇒ { fieldName = s"Also added a comment:" }
            case (true, false) ⇒ { fieldName = s"Also edited a comment:" }
            case (false, true) ⇒ {
              message = s"${updatedBy} added a comment to ${issueType} <${issueLink}|${issueName}> (${fields.summary})."
            }
            case (false, false) ⇒ {
              message = s"${updatedBy} edited a comment to ${issueType} <${issueLink}|${issueName}> (${fields.summary})."
            }
          }

          attachmentsBuffer += IncomingWebHookAttachment(
            s"${fieldName}: ${comment.body}", None, None, jira.colors.comment,
            List(IncomingWebHookAttachmentField(fieldName, comment.body))
          )
        }

        val attachments = attachmentsBuffer.result
        val attachmentsOpt = if (attachments.isEmpty) { None } else { Some(attachments) }

        IncomingWebHook(message, username, channel, iconUrl, None, attachmentsOpt).send
      }
    )

    Ok
  }
}

