package io.slackoff.zik

trait ZikConfig extends io.slackoff.core.utils.Config {
  isModule("zik")

  object zik {
    object soundcloud {
      def enabled = config.getBoolean(configPrefix + "soundcloud.enabled") getOrElse false
      def clientId = config.getString(configPrefix + "soundcloud.clientId") getOrElse ""
    }

    object spotify {
      def enabled = config.getBoolean(configPrefix + "spotify.enabled") getOrElse false
    }
  }
}
