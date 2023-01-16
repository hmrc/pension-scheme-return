import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.12.0"
  private val hmrcMongoVersion = "0.74.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion            % "test, it",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % hmrcMongoVersion            % Test,
    "org.scalatestplus"       %% "scalacheck-1-15"            % "3.2.11.0"                  % "test, it",
    "org.scalacheck"          %% "scalacheck"                 % "1.17.0"                    % "test, it",
    "org.mockito"             %% "mockito-scala"              % "1.17.12"                   % "test, it",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current         % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.64.0"                    % "it",
  )
}
