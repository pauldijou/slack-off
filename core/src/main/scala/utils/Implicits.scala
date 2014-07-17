package io.slackoff.core
package utils

import org.joda.time.Duration

import play.api.libs.json._
import play.api.libs.functional.syntax._

object Implicits {
  implicit val durationReads = new Reads[Duration] {
    def reads(json: JsValue): JsResult[Duration] = json match {
      case JsNumber(d) => JsSuccess(new Duration(d.toLong))
      case _ => JsError(JsPath(), "error.expected.duration")
    }
  }

  implicit val durationWrites = new Writes[Duration] {
    def writes(duration: Duration): JsValue = JsNumber(duration.getMillis)
  }
}
