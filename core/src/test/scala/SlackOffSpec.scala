package io.slackoff.core.test

import scala.concurrent.Future

import org.scalatest._
import org.scalatestplus.play._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import io.slackoff.core.models._

trait SlackOffSpec extends PlaySpec {
    def prefix: String = ""

    val outgoingRequestParams= Seq(
      "token" -> "azerty",
      "team_id" -> "T123",
      "team_domain" -> "Slack",
      "channel_id" -> "C123",
      "channel_name" -> "hack_and_slack",
      "timestamp" -> "1355517523.000005",
      "user_id" -> "U123",
      "user_name" -> "slacky",
      "text" -> "Here I come",
      "trigger_word" -> "!"
    )

    object Requests {
      object outgoing {
        val valid = FakeRequest("POST", "/outgoings").withFormUrlEncodedBody(outgoingRequestParams: _*)
        val invalid = FakeRequest("POST", "/outgoings").withFormUrlEncodedBody(outgoingRequestParams.drop(2): _*)
        val wrongUrl = FakeRequest("POST", "/toto").withFormUrlEncodedBody(outgoingRequestParams: _*)
      }
    }
}
