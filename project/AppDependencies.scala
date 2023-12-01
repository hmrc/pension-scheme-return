import play.core.PlayVersion
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "8.1.0"
  private val hmrcMongoVersion = "1.6.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "org.typelevel"                 %% "cats-core"                  % "2.10.0",
    "com.networknt"                 %  "json-schema-validator"      % "1.0.87" exclude ("org.slf4j", "slf4j-api"),
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.16.0"
  )

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"            %% "play-test"                   % PlayVersion.current         % Test,
    "uk.gov.hmrc"                  %% "bootstrap-test-play-28"      % bootstrapVersion            % "test, it",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-test-play-28"     % hmrcMongoVersion            % Test,
    "org.scalatestplus"            %% "scalacheck-1-17"             % "3.2.17.0"                  % "test, it",
    "org.scalacheck"               %% "scalacheck"                  % "1.17.0"                    % "test, it",
    "org.mockito"                  %% "mockito-scala"               % "1.17.30"                   % "test, it",
    "com.vladsch.flexmark"         %  "flexmark-all"                % "0.64.6"                    % "it"
  )
}
