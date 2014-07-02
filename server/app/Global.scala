import play.api._
import play.api.mvc._

import controllers._

object Global extends GlobalSettings {
  def default(req: RequestHeader): Handler = super.onRouteRequest(req) getOrElse Action { Results.NotFound }

  override def onRouteRequest(req: RequestHeader): Option[Handler] = (req.method, req.path) match {
    case ("GET", "/debug") => Option(Debug.display)
    case _ => Option(io.slackoff.core.controllers.SlackOffController.applyRoute(req, default))
  }
}
