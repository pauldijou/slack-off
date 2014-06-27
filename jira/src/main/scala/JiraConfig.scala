package io.slackoff.jira

trait JiraConfig extends io.slackoff.core.utils.Config {
  object jira {
    def url = getString("slackoff.jira.url")
    def authBasic = getString("slackoff.jira.auth.basic")
    def blackList = getStringSeq("slackoff.jira.blacklist")

    object bot {
      def name = config.getString("slackoff.jira.bot.name")
      def icon = config.getString("slackoff.jira.bot.icon")
    }

    object colors {
      object issue {
        def created = config.getString("slackoff.jira.colors.issue.created")
        def updated = config.getString("slackoff.jira.colors.issue.updated")
        def deleted = config.getString("slackoff.jira.colors.issue.deleted")
      }

      def comment = config.getString("slackoff.jira.colors.comment")
    }

    object worklog {
      def enabled = config.getBoolean("slackoff.jira.worklog.enabled") getOrElse true
    }
  }
}
