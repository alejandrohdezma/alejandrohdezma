ThisBuild / scalaVersion             := "2.13.6"
ThisBuild / organization             := "com.alejandrohdezma"
ThisBuild / Test / parallelExecution := false

addCommandAlias("ci-test", "fix --check; mdoc; Test / compile; test")

Global / filesToGenerate -= ".github/workflows/release.yml"
Global / filesToGenerate := (Global / filesToGenerate).value.filterNot(_.startsWith("docs"))

lazy val `munit` = module
  .settings(libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test)
  .settings(libraryDependencies += "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test)
  .settings(libraryDependencies += "org.typelevel" %% "munit-cats-effect-3" % "1.0.6" % Test)
  .settings(libraryDependencies += "com.alejandrohdezma" %% "http4s-munit" % "0.9.1" % Test)
  .settings(libraryDependencies += "com.dimafeng" %% "testcontainers-scala-munit" % "0.39.10" % Test)
  .settings(libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.6" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-async-http-client" % "0.23.6" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-client" % "0.23.6" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-dsl" % "0.23.6" % Test)

lazy val site = project
  .enablePlugins(MdocPlugin)
  .settings(mdocIn := baseDirectory.value / "docs")
  .settings(watchTriggers ++= List(mdocIn.value.toGlob / "*.md", mdocIn.value.toGlob / "*.html"))
  .enablePlugins(GitHubPagesPlugin)
  .settings(gitHubPagesSiteDir := mdocOut.value)
