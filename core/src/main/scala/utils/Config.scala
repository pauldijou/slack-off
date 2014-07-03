package io.slackoff.core
package utils

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import play.api.Play
import play.api.Configuration

import io.slackoff.core.models.Team

trait Config {
  def config = Config.config
  def getString(key: String) = Config.getString(key)
  def getStringSeq(key: String) = Config.getStringSeq(key)
  def getConfigSeq(key: String) = Config.getConfigSeq(key)
  def getConfigList(key: String) = Config.getConfigList(key)

  var configPrefix = "slackoff."

  def isModule(name: String) = {
    configPrefix += name + "."
  }

  // MESSAGES
  object messages {
    object jira {
      def enabled = config.getBoolean("slackoff.messages.jira.enabled") getOrElse false
      def regex = getString("slackoff.messages.jira.regex")
    }
  }

  // CORE
  object core {
    def teams = Config.teams
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
  def asScalaSeq[A](l: java.util.List[A]): Seq[A] = asScalaBufferConverter(l).asScala.toSeq
  def asScalaList[A](l: java.util.List[A]): List[A] = asScalaBufferConverter(l).asScala.toList

  lazy val config = Play.current.configuration

  def getString(key: String, configuration: Configuration = config): String =
    configuration.getString(key) getOrElse ""

  def getStringSeq(key: String, configuration: Configuration = config): Seq[String] =
    configuration.getStringList(key).map(asScalaSeq) getOrElse Seq.empty

  def getConfigList(key: String, configuration: Configuration = config): List[Configuration] =
    (configuration.getConfigList(key).map(asScalaList) getOrElse List.empty).toList

  def getConfigSeq(key: String, configuration: Configuration = config): Seq[Configuration] =
    configuration.getConfigSeq(key) getOrElse Seq.empty

  def getConfigSeqFromEnv(key: String): Seq[Configuration] =
    config
      .getString(key)
      .map { s => ConfigFactory.parseString("teams=" + s) }
      .map { c => Configuration(c) }
      .map { c => getConfigSeq("teams", c) }
      .getOrElse { Seq.empty }

  lazy val teams: Seq[Team] = getConfigSeqFromEnv("slackoff.teams")
    .map { o => Team(
      o.getString("id").getOrElse(""),
      o.getString("name").getOrElse(""),
      o.getStringSeq("modules"),
      o.getConfig("tokens").map { _.keys }.getOrElse(Set.empty).map { key => (key -> o.getStringSeq("tokens." + key).getOrElse(Seq.empty)) }.toMap
    ) }
}
