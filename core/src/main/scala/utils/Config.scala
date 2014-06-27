package io.slackoff.core
package utils

import scala.collection.JavaConverters._
import play.api.Play

trait Config {
  def config = Config.config
  def getString(key: String) = Config.getString(key)
  def getStringSeq(key: String) = Config.getStringSeq(key)

  // MESSAGES
  object messages {
    object jira {
      def enabled = config.getBoolean("slackoff.messages.jira.enabled") getOrElse false
      def regex = getString("slackoff.messages.jira.regex")
    }
  }

  // SLACK
  object slack {
    object team {
      def name = getString("slackoff.slack.team.name")
    }

    object hooks {
      object incoming {
        def token = getString("slackoff.slack.hooks.incoming.token")
      }
    }
  }
}

object Config {
  def asScalaList[A](l: java.util.List[A]): Seq[A] = asScalaBufferConverter(l).asScala.toList
  
  lazy val config = Play.current.configuration
  def getString(key: String): String = config.getString(key) getOrElse ""
  def getStringSeq(key: String): Seq[String] = config.getStringList(key).map(asScalaList) getOrElse Seq.empty
}
