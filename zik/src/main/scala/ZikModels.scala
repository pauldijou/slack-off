package io.slackoff.zik

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Provider(name: String)

case class Zik(title: String)

object Zik {
  implicit val zikFormat = Json.format[Zik]
}
