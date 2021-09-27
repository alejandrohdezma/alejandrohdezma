ThisBuild / scalaVersion             := "2.13.6"
ThisBuild / organization             := "com.alejandrohdezma"
ThisBuild / Test / parallelExecution := false

addCommandAlias("ci-test", "scalafmtAll; scalafmtSbt; mdoc; compile")

lazy val `munit` = module
  .settings(libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test)
  .settings(libraryDependencies += "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test)
  .settings(libraryDependencies += "org.typelevel" %% "munit-cats-effect-2" % "1.0.5" % Test)
  .settings(libraryDependencies += "com.alejandrohdezma" %% "http4s-munit" % "0.9.0" % Test)
  .settings(libraryDependencies += "com.dimafeng" %% "testcontainers-scala-munit" % "0.39.8" % Test)
  .settings(libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.6" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-async-http-client" % "0.23.4" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-client" % "0.23.4" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-dsl" % "0.23.4" % Test)

lazy val site = project
  .enablePlugins(MdocPlugin)
  .settings(mdocIn := baseDirectory.value / "docs")
  .settings(watchTriggers ++= List(mdocIn.value.toGlob / "*.md", mdocIn.value.toGlob / "*.html"))
  .enablePlugins(GitHubPagesPlugin)
  .settings(gitHubPagesSiteDir := mdocOut.value)
