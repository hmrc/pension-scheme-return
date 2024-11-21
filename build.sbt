import uk.gov.hmrc.DefaultBuildSettings

val appName = "pension-scheme-return"

inThisBuild(
  List(
    scalaVersion := "3.5.2",
    majorVersion := 0,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalafmtOnCompile := true,
    scalafixOnCompile := true,
    PlayKeys.playDefaultPort := 10700
  )
  .settings(scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-Wconf:msg=unused import&src=conf/.*:s",
    "-Wconf:msg=Flag.*repeatedly:s",
    "-Wconf:src=routes/.*:s")
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

addCommandAlias("testc", "; clean ; coverage ; test ; it/test ; coverageReport ;")
