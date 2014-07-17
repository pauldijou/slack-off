package io.slackoff.zik

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import io.slackoff.core.Api
import io.slackoff.core.models._

object ZikController
    extends io.slackoff.core.controllers.ModuleController
    with ZikConfig {

  lazy val logger = initLogger("slackoff.modules.zik.controller")

  def hasRoute(rh: RequestHeader) = true

  def applyRoute[RH <: RequestHeader, H >: Handler](rh: RH, default: RH ⇒ H) =
    (rh.method, rh.path.drop(path.length)) match {
      case ("GET", "")  ⇒ Action { Ok("JIRA plugin is currently running...") }
      case ("POST", "") ⇒ Action { Ok("JIRA plugin is currently running...") }
      case _            ⇒ default(rh)
    }
}
