import akka.actor.{ActorSystem, Scheduler}
import org.json4s.DefaultFormats
import org.slf4j.{Logger, LoggerFactory}

package object classifier {
  val logger: Logger = LoggerFactory.getLogger(getClass)
  val as: ActorSystem = ActorSystem("blocks-scheduler")
  implicit val scheduler: Scheduler = as.scheduler
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
}