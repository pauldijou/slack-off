package io.slackoff.zik

import org.joda.time.Duration

import play.api.libs.json._
import play.api.libs.functional.syntax._

import io.slackoff.core.models.User
import io.slackoff.core.utils.Implicits._

case class Provider(name: String)

case class Zik(title: String, duration: Duration)

object Zik {
  implicit val zikFormat = Json.format[Zik]
}

case class QueueItem(zik: Zik, user: User)

case class Queue(current: QueueItem, next: Seq[QueueItem], previous: Seq[QueueItem], playing: Boolean)

case class Profile(user: User, favorites: Seq[Zik], lastSearch: Seq[Zik])

case class State(queue: Queue, users: Map[String, Profile])
