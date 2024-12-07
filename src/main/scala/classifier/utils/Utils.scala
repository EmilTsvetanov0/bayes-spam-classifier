package classifier.utils

import org.json4s.Formats
import org.json4s._
import org.json4s.jackson.JsonMethods._

object Utils {
  implicit val formats: Formats = org.json4s.DefaultFormats.withLong.withDouble.withStrictOptionParsing

  def map2json[T <: AnyRef](cls: T): String = org.json4s.native.Serialization.write(cls)

  def extractField(request: String, field: String): String = {
    val parsedJson: JValue = parse(request)
    (parsedJson \ field).extractOpt[String].getOrElse("")
  }
}