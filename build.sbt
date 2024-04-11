import uk.gov.hmrc.DefaultBuildSettings

val appName = "pension-scheme-return"

inThisBuild(
  List(
    scalaVersion := "2.13.12",
    majorVersion := 0,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := "2.13"
  )
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalafmtOnCompile := true,
    scalafixOnCompile := true,
    PlayKeys.playDefaultPort := 10700
  )
  .settings(inConfig(Test)(testSettings) *)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings *)

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork := true,
  Test / scalafmtOnCompile := true,
  Test / scalafixOnCompile := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it = project
  .in(file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    libraryDependencies ++= AppDependencies.test,
    Test / fork := true,
    Test / scalafmtOnCompile := true,
    Test / unmanagedResourceDirectories += baseDirectory.value / "it" / "test" / "resources"
  )
