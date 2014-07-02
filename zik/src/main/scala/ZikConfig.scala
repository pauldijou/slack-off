package io.slackoff.zik

trait ZikConfig extends io.slackoff.core.utils.Config {
  isModule("zik")

  object zik {
    object soundcloud {
      def enabled = config.getBoolean(configPrefix + "soundcloud.enabled") getOrElse false
    }

    object spotify {
      def enabled = config.getBoolean(configPrefix + "spotify.enabled") getOrElse false
    }
  }
}
