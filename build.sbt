import Dependencies._
import Resolvers._
import _root_.sbt.{Resolvers => _, _}


lazy val buildSettings = Seq(
  organization := "shovel.edu",
  name := "bayes-spam-classifier",
  version := "0.1",
  scalaVersion := "2.13.10"
)

javacOptions ++= Seq("-encoding", "UTF-8")

scalacOptions ++= Seq(
  "-deprecation",
  "-Wconf:cat=other-match-analysis:error"
)

lazy val main = (project in file("."))
  .settings(
    buildSettings,
    libraryDependencies ++= compileDependencies,
    useCoursier := false
  )
