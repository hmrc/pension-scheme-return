import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.2.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "org.typelevel"                 %% "cats-core"                  % "2.12.0",
    "com.networknt"                 %  "json-schema-validator"      % "1.5.3" exclude ("org.slf4j", "slf4j-api"),
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.18.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-test-play-30"      % bootstrapVersion,
    "org.scalatestplus"            %% "scalacheck-1-18"             % "3.2.19.0",
    "com.softwaremill.diffx"       %% "diffx-scalatest-should"      % "0.9.0"
  ).map(_ % Test)
}
