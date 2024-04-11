import sbt.*

object AppDependencies {

  private val bootstrapVersion = "8.5.0"
  private val hmrcMongoVersion = "1.8.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "org.typelevel"                 %% "cats-core"                  % "2.10.0",
    "com.networknt"                 %  "json-schema-validator"      % "1.4.0" exclude ("org.slf4j", "slf4j-api"),
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.17.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-test-play-30"      % bootstrapVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-test-play-30"     % hmrcMongoVersion,
    "org.scalatestplus"            %% "scalacheck-1-17"             % "3.2.18.0",
    "com.softwaremill.diffx"       %% "diffx-scalatest-should"      % "0.9.0"
  ).map(_ % Test)
}
