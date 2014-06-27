package io.slackoff.core
package utils

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import play.api._
import play.api.mvc._
import play.api.mvc.Results.Ok

trait Answer {
  def ok = Ok
  def ok(content: String) = Ok(content)
  def asyncOk = Future.successful(ok)
  def asyncOk(content: String) = Future.successful(ok(content))

  def error(content: String) = ok("ERROR: " + content)
  def asyncError(content: String) = asyncOk("ERROR: " + content)
}

