import _root_.sbt.{Resolvers => _, _}


object Dependencies {
  private val akkaVersion = "2.8.0"
  private val akkaHttpVersion = "10.5.0"
  private val prometheusMetricsVersion = "0.12.0"
  private val rules = List(ExclusionRule(organization = "ch.qos.logback"), ExclusionRule(organization = "org.slf4j"))

  def compileDependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,

    "ch.qos.logback" % "logback-core" % "1.2.3",
    "ch.qos.logback" % "logback-classic" % "1.2.3",

    "org.json4s" %% "json4s-native" % "4.0.2",
    "org.json4s" %% "json4s-ext" % "4.0.2",
    "org.json4s" %% "json4s-core" % "4.0.6",
    "org.json4s" %% "json4s-jackson" % "4.0.6",

    "commons-io" % "commons-io" % "2.7",
    "net.logstash.logback" % "logstash-logback-encoder" % "6.4",
    "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
    "com.auth0" % "java-jwt" % "3.10.3",
    "org.scala-lang" %% "toolkit" % "0.2.0",

    "edu.stanford.nlp" % "stanford-corenlp" % "4.5.7"
  )
}
