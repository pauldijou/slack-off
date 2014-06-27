package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._


object Debug extends Controller with io.slackoff.core.utils.Config {

  def display = Action {
    val prints = Map(
      "slack.team.name" -> slack.team.name
    )

    Ok(views.html.debug(prints))
  }

}
