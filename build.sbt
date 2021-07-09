ThisBuild / scalaVersion             := "2.13.4"
ThisBuild / organization             := "com.alejandrohdezma"
ThisBuild / testFrameworks           += new TestFramework("munit.Framework")
ThisBuild / Test / parallelExecution := false

lazy val `munit` = module
  .settings(libraryDependencies += "org.scalameta" %% "munit" % "0.7.22" % Test)
  .settings(libraryDependencies += "org.scalameta" %% "munit-scalacheck" % "0.7.22" % Test)
  .settings(libraryDependencies += "org.typelevel" %% "munit-cats-effect-2" % "0.13.1" % Test)
  .settings(libraryDependencies += "com.alejandrohdezma" %% "http4s-munit" % "0.6.0" % Test)
  .settings(libraryDependencies += "com.dimafeng" %% "testcontainers-scala-munit" % "0.39.3" % Test)
  .settings(libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-async-http-client" % "0.21.20" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-client" % "0.21.20" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-dsl" % "0.21.20" % Test)

lazy val site = project
  .enablePlugins(MdocPlugin)
  .settings(mdocIn := baseDirectory.value / "docs")
  .settings(watchTriggers ++= List(mdocIn.value.toGlob / "*.md", mdocIn.value.toGlob / "*.html"))
  .enablePlugins(GitHubPagesPlugin)
  .settings(gitHubPagesSiteDir := mdocOut.value)
