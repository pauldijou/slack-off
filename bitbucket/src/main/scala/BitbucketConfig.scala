package io.slackoff.bitbucket
package utils

trait BitbucketConfig extends io.slackoff.core.utils.Config {
  object bitbucket {
    object bot {
      def name = config.getString("slackoff.bitbucket.bot.name")
      def icon = config.getString("slackoff.bitbucket.bot.icon")
    }
  }
}
