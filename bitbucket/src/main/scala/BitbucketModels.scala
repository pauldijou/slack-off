package io.slackoff.bitbucket
package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

// COMMIT FILE
case class BitbucketCommitFile(file: String, fileType: String)
object BitbucketCommitFile {
  implicit val bitbucketCommitFileRead = (
    (__ \ "file").read[String] and
    (__ \ "type").read[String]
  )(apply _)

  implicit val bitbucketCommitFileWrite = Json.writes[BitbucketCommitFile]
}

// COMMIT
case class BitbucketCommit(
  author: String,
  branch: Option[String],
  branches: Option[List[String]],
  files: List[BitbucketCommitFile],
  message: String,
  node: String,
  parents: List[String],
  raw_author: String,
  raw_node: String,
  revision: Option[String],
  size: Long,
  timestamp: String,
  utctimestamp: String
) {
  def isMerge = this.branches.map(_.size > 1).getOrElse(false)
  def browseUrl(projectUrl: String) = s"${projectUrl}commits/${this.raw_node}"
  def browseAuthorUrl(canonUrl: String) = s"${canonUrl}/${this.author}"
  def browseBranchUrl(projectUrl: String) = this.branch.map(b => s"${projectUrl}branch/${b}").getOrElse("")
  def browseBranchCommitsUrl(projectUrl: String) = this.branch.map(b => s"${projectUrl}commits/branch/${b}").getOrElse("")
}
object BitbucketCommit { implicit val bitbucketCommitFormat = Json.format[BitbucketCommit] }

// REPOSITORY
case class BitbucketRepository(
  absolute_url: String,
  fork: Boolean,
  is_private: Boolean,
  name: String,
  owner: String,
  scm: String,
  slug: String,
  website: String
)
object BitbucketRepository { implicit val bitbucketRepositoryFormat = Json.format[BitbucketRepository] }

// POST HOOK
case class BitbucketPostHook(canon_url: String, commits: List[BitbucketCommit], repository: BitbucketRepository, user: String)
object BitbucketPostHook { implicit val bitbucketPostHookFormat = Json.format[BitbucketPostHook] }
