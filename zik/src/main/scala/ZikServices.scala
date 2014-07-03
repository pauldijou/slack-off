package io.slackoff.zik

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.json._

object ZikServices extends ZikConfig {
  object soundcloud {
    val endpoint = "http://api.soundcloud.com"
    val format = ".json"
    val clientId = zik.soundcloud.clientId

    def caller(section: String, query: (String, String)*): WSRequestHolder =
      WS.url(s"${endpoint}${section}${format}").withQueryString("client_id" -> clientId).withQueryString(query: _*)

    def handleResponse[A](response: WSResponse)(implicit reader: Reads[A]) = response.status match {
      case 200 => Json.fromJson[A](response.json) getOrElse { throw new Exception(s"Soundloud returned invalid JSON}") }
      case _ => throw new Exception(s"Soundloud returned status ${response.status}")
    }

    object tracks {
      def search(q: String): Future[Seq[Zik]] = caller("/tracks", "q" -> q).get().map(handleResponse[Seq[Zik]])
    }
  }
}
