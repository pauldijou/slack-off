package io.slackoff.core
package models

case class Team(id: String, name: String, modules: Option[Seq[String]], tokens: Map[String, Seq[String]])
