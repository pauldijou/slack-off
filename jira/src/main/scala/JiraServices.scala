package io.slackoff.jira

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WS
import play.api.Play.current

import io.slackoff.core.utils.Numbers
import io.slackoff.jira._

object JiraServices extends JiraConfig {
  lazy val apiUrl = jira.url + "/rest/api/2"
  lazy val apiUrlIssues = apiUrl + "/issue"

  lazy val api = WS.url(jira.url + "/rest/api/2") //.withHeaders("Authorization" -> ("Basic " + jiraAuthBasic))
  lazy val apiIssues = WS.url(apiUrlIssues) //.withHeaders("Authorization" -> ("Basic " + jiraAuthBasic))

  def issueUrl(key: String) = jira.url + "/browse/" + key

  def get(key: String): Future[Option[JiraIssue]] =
    WS.url(apiUrlIssues + "/" + key)
      .withHeaders("Authorization" -> ("Basic " + jira.authBasic))
      .get.map(_.json.asOpt[JiraIssue])

  def create(summary: String, description: String, project: String, issueType: String): Future[JsObject] = {
    val projectLabel = if (Numbers.isAllDigits(project)) { "id" } else { "key" }
    val issueTypeLabel = if (Numbers.isAllDigits(issueType)) { "id" } else { "name" }

    val newIssue = Json.obj(
      "fields" -> Json.obj(
        "project" -> Json.obj(projectLabel -> project),
        "summary" -> summary,
        "description" -> description,
        "issuetype" -> Json.obj(issueTypeLabel -> issueType)
      )
    )

    apiIssues.post(newIssue).map(_.json.as[JsObject])
  }
}
