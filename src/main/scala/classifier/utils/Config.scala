package classifier.utils

import java.io.FileInputStream
import java.util.Properties
import ch.qos.logback.classic.util.ContextInitializer

import classifier.domain.Domain

object Config {

  private val p = new Properties

  def loadConfigs(): Unit = {
    p.load(new FileInputStream("conf.d/server.properties"))
    System.setProperty("config.file", "conf.d/application.conf")
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "conf.d/logback.xml")
  }

  def getApiHost: String = getStringParam("api_host", "0.0.0.0")

  def getApiPort: Int = getStringParam("api_port", "3141").toInt

  def getActorSystemName: String =
    getStringParam("actor_system_name", "GuardianV2ActorSystem")

  def futureTimeout: Int = getStringParam("future_timeout", "250").toInt

  // ------------------------------------------------------------
  // BayesClassifier
  // ------------------------------------------------------------

  def getModelPath: String = "src/main/resources/model.json"

  // Уже известная информация, неудобно хранить в отдельном файле
  def getModelCounts: Domain.ModelCounts = Domain.ModelCounts(15797, 34793, 1434, 1434, 10227)

  // ------------------------------------------------------------
  // Utils
  // ------------------------------------------------------------

  private def getParam(paramName: String): String = {
    val paramValue = p.getProperty(paramName)
    if (paramValue == null)
      sys.error(s"no param with name $paramName in conf.d/server.properties")
    else paramValue.trim
  }

  private def getStringParam(paramName: String, defaultValue: String = ""): String = {
    val paramValue = p.getProperty(paramName)
    if (paramValue == null) defaultValue else paramValue.trim
  }
}
