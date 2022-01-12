ThisBuild / scalaVersion             := "2.13.7"
ThisBuild / organization             := "com.alejandrohdezma"
ThisBuild / Test / parallelExecution := false

addCommandAlias("ci-test", "fix --check; headerCheckAll; test")

Global / filesToGenerate := (Global / filesToGenerate).value
  .filterNot(_.startsWith("docs"))
  .filterNot(_.contains("release"))

lazy val `munit` = module
  .settings(libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test)
  .settings(libraryDependencies += "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test)
  .settings(libraryDependencies += "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test)
  .settings(libraryDependencies += "com.alejandrohdezma" %% "http4s-munit" % "0.9.2" % Test)
  .settings(libraryDependencies += "com.dimafeng" %% "testcontainers-scala-munit" % "0.39.12" % Test)
  .settings(libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-async-http-client" % "0.23.7" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-client" % "0.23.7" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-dsl" % "0.23.7" % Test)
