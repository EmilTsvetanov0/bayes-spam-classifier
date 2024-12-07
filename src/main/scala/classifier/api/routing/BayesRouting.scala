package classifier.api.routing

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import org.slf4j.{Logger, LoggerFactory}

import classifier.domain.Domain._
import classifier.utils.{ScanUtils, Utils}
import classifier.api.services.BayesClassifierService

object BayesRouting {
  val log: Logger = LoggerFactory.getLogger(getClass)

  val bayesCheckRoute: Route = (post & path("checkBayes") & entity(as[String])) { request =>
    val either: Either[Error,Map[String, Any]] = {
      Utils.extractField(request, "text") match {
        case "" => Left(Error(code = 400, description = "bad request"))
        case text =>
          Right({
            val (isSpam, spamProb, hamProb) = BayesClassifierService.isSpam(ScanUtils.preprocessAndCount(text))
            Map[String,Any](
            "isSpam" -> isSpam,
            "spamProb" -> spamProb,
            "hamProb" -> hamProb
            )
          })
      }
    }
    Routing.completeEither(either)
  }
}
