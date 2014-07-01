package io.slackoff.core
package controllers

import play.api._
import play.api.mvc._

import io.slackoff.core.Api
import io.slackoff.core.controllers._

object SlackOffController
    extends io.slackoff.core.controllers.ModuleController
    with io.slackoff.core.utils.Config {

  lazy val logger = Logger("core.controllers.slackoff")

  def hasRoute(rh: RequestHeader) = true

  def applyRoute[RH <: RequestHeader, H >: Handler](rh: RH, default: RH ⇒ H) = {
    val subpath = rh.path.drop(path.length)
    val paths = subpath.split("/").filter { _ != "" }.toSeq.lift

    (rh.method, paths(0), paths(1)) match {
      // Root route
      case ("GET", None, None)  ⇒ Action { Ok(io.slackoff.html.index(Api.getModules, path + "/modules/")) }
      // Registered modules routes
      case (_, Some("modules"), Some(moduleName)) ⇒ {
        Api.getModule(moduleName) match {
          case Some(module) ⇒ module.controller.map { c =>
            c.setPrefix(path + "/modules/" + moduleName)
            c.applyRoute(rh, default)
          } getOrElse default(rh)
          case _            ⇒ default(rh)
        }
      }
      // Whatever...
      case _ ⇒ default(rh)
    }
  }
}
