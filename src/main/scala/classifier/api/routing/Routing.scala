package classifier.api.routing

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import org.slf4j.{Logger, LoggerFactory}

import scala.language.postfixOps

import classifier.api.routing.BayesRouting._
import classifier.utils.Utils
import classifier.domain.Domain

object Routing {
  val log: Logger = LoggerFactory.getLogger(getClass)

  private val pingRoute: Route = (get & path("ping")) {
    complete(StatusCodes.OK)
  }

  val routes: Route = concat(pingRoute, bayesCheckRoute)

  private def completeJson(result: String): StandardRoute = {
    val httpEntity = HttpEntity(ContentType(MediaType.applicationWithOpenCharset("json"), HttpCharsets.`UTF-8`), result)
    complete(HttpResponse(entity = httpEntity))
  }

  def completeEither(either: Either[Domain.Error, Any]): StandardRoute = {
    completeJson(wrapResult(either))
  }

  protected def wrapOkResult(data: Any): String =
    Utils.map2json(Map("success" -> true, "result" -> data, "t" -> System.currentTimeMillis))

  protected def wrapErrorResult(error: Domain.Error): String =
    Utils.map2json(Map("success" -> false, "error" -> error, "t" -> System.currentTimeMillis))

  private def wrapResult(result: Either[Domain.Error, Any]): String = result match {
    case Right(data) =>
      val result = wrapOkResult(data)
      log.info(s"Response - object: $data json: $result")
      result
    case Left(error) =>
      val resultError = wrapErrorResult(error)
      log.error(s"Response error: $resultError")
      resultError
  }
}
