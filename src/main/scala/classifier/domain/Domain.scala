package classifier.domain

import scala.language.{existentials, implicitConversions, postfixOps}

object Domain {

  case class Error(code: Int, description: String, info: Option[Map[String, Any]] = None)

  object Error {
    def fromMap(msa: Map[String, Any]): Error = Error(
      msa.get("code").map(_.toString.toInt).getOrElse(500),
      msa.get("description").map(_.toString).getOrElse("Internal error")
    )
  }

  case class ModelCounts(
                          spamWords: Long,
                          hamWords: Long,
                          spamPages: Long,
                          hamPages: Long,
                          uniqueWords: Long
                        )
}