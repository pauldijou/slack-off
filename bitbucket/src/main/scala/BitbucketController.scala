package io.slackoff.bitbucket
package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import io.slackoff.core.Api
import io.slackoff.core.models._
import io.slackoff.bitbucket.models._

object BitbucketController
  extends io.slackoff.core.controllers.ModuleController
  with io.slackoff.bitbucket.utils.BitbucketConfig {

  lazy val logger = initLogger("slackoff.modules.bitbucket.controller")

  def hasRoute(rh: RequestHeader) = true

  def applyRoute[RH <: RequestHeader, H >: Handler](rh: RH, default: RH => H) =
    (rh.method, rh.path.drop(path.length)) match {
      case ("POST", "") => handlePostHook
      case _ => default(rh)
    }

  def handlePostHook = Action { implicit request =>
    debugStart("Bitbuckets.handlePostHook")
    debug(request.body.toString)

    val body: JsValue = request.body match {
      case json: AnyContentAsJson => json.asJson.getOrElse(JsUndefined("Request body is not valid JSON."))
      case form: AnyContentAsFormUrlEncoded => {
        (for {
          mapForm <- form.asFormUrlEncoded
          payload <- mapForm.get("payload")
          payloadContent <- payload.headOption
        } yield {
          Json.parse(payloadContent)
        }) getOrElse(JsUndefined("Request body payload is not valid JSON."))
      }
      case _ => JsUndefined("Body type not supported")
    }

    debug(Json.prettyPrint(body))

    body.validate[BitbucketPostHook].fold(
      errors => debug(errors),
      hook => {
        debug(hook.toString)
        val team = request.getQueryString("team") map { Api.teams.from(_) } getOrElse Api.teams.default
        val username = request.getQueryString("username") orElse bitbucket.bot.name
        val channel = request.getQueryString("channel").map("#" + _)
        val iconUrl = request.getQueryString("iconUrl") orElse bitbucket.bot.icon

        // Thanks to Altassian guys, if you push several commits at once in the same branch,
        // only the last one will have a non-null branch property in the payload.
        // We need to normalize that!
        var branches = scala.collection.mutable.Map[String, String]()
        val commits: List[BitbucketCommit] = (hook.commits.reverse.map { commit => commit.branch match {
          // Yeah, we have a branch! Don't touch the commit
          // but let's save its branch and also consider its parents might probably
          // be on the same branch
          case Some(b) => {
            branches += (commit.node -> b)
            commit.parents.foreach { parent => branches += (parent -> b) }
            commit
          }
          // Ouch, no branch... that shouldn't be possible, right?
          // Anyway, let's hope one of its children correctly put a branch in the map
          case None => branches.get(commit.node) match {
            // Woot, we find one! Same has before, get ready to propagate to parents if necessary
            case Some(b) => {
              commit.parents.foreach { parent => branches += (parent -> b) }
              commit.copy(branch = Some(b))
            }
            // Let's hope we never reach here...
            case None => commit
          }
        }}).reverse

        val projectUrl = s"${hook.canon_url}${hook.repository.absolute_url}"
        val commitsPlural = if (commits.size > 1) { "s" } else { "" }
        val totalFiles = commits.foldLeft(0) { (acc, c) => acc + c.files.size }
        val totalFilesPlural = if (totalFiles > 1) { "s" } else { "" }
        val message = s"[<${projectUrl}|${hook.repository.name}>] ${commits.size} commit${commitsPlural} impacting ${totalFiles} file${totalFilesPlural}"
        var commitsBuffer = scala.collection.mutable.ListBuffer[IncomingWebHookAttachment]()

        commits foreach { commit =>
          val filesPlural = if (commit.files.size > 1) { "s" } else { "" }
          val filesMsg = s"${commit.files.size} file${filesPlural} impacted"
          val commitLink = s"<${commit.browseUrl(projectUrl)}|${commit.node}>"
          val authorName = commit.raw_author.replaceAll("<[^>]*>", "").trim
          val authorLink = s"<${commit.browseAuthorUrl(hook.canon_url)}|${authorName}>"
          val branchLink = commit.branch.map(b => s"<${commit.browseBranchCommitsUrl(projectUrl)}|${b}>").getOrElse("no branch")

          val msg = s"[${branchLink} / ${commitLink}] ${authorLink}: ${commit.message}"
          commitsBuffer += IncomingWebHookAttachment(msg, Some(msg), None, None, List())
        }

        val attachments = commitsBuffer.result
        val attachmentsOpt = if (attachments.isEmpty) { None } else { Some(attachments) }

        IncomingWebHook(message, username, channel, iconUrl, None, attachmentsOpt).send(team)
      }
    )

    debugEnd
    Ok
  }
}
