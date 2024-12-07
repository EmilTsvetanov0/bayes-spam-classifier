package classifier

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps

import classifier.utils.Config
import classifier.api.routing.Routing

object Boot {

  Config.loadConfigs()
  implicit val system: ActorSystem = ActorSystem(Config.getActorSystemName)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def main(args: Array[String]): Unit = {
    startService()
  }

  private def startService(): Unit = {
    val host = Config.getApiHost
    val port = Config.getApiPort

    classifier.logger.info("""
         |███████╗██╗  ██╗ ██████╗ ██╗   ██╗███████╗██╗         ███████╗
        | ██╔════╝██║  ██║██╔═══██╗██║   ██║██╔════╝██║         ██╔════╝
        | ███████╗███████║██║   ██║██║   ██║█████╗  ██║         █████╗
        | ╚════██║██╔══██║██║   ██║██║   ██║██╔══╝  ██║         ██╔══╝
        | ███████║██║  ██║╚██████╔╝╚██████╔╝███████╗███████╗    ███████╗
        | ╚══════╝╚═╝  ╚═╝ ╚═════╝  ╚═════╝ ╚══════╝╚══════╝    ╚══════╝
        |""".stripMargin)
    classifier.logger.info(s"TalkTo Site API is up at $host:$port")

    Http().newServerAt(host, port).bind(Routing.routes)


  }
}
