ThisBuild / scalaVersion             := "2.13.4"
ThisBuild / organization             := "com.alejandrohdezma"
ThisBuild / testFrameworks           += new TestFramework("munit.Framework")
ThisBuild / Test / parallelExecution := false

lazy val `munit` = module
  .settings(libraryDependencies += "org.scalameta" %% "munit" % "0.7.19" % Test)
  .settings(libraryDependencies += "org.scalameta" %% "munit-scalacheck" % "0.7.20" % Test)
  .settings(libraryDependencies += "org.typelevel" %% "munit-cats-effect-2" % "0.11.0" % Test)
  .settings(libraryDependencies += "com.alejandrohdezma" %% "http4s-munit" % "0.5.1" % Test)
  .settings(libraryDependencies += "com.dimafeng" %% "testcontainers-scala-munit" % "0.38.9" % Test)
  .settings(libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-async-http-client" % "0.21.20" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-client" % "0.21.20" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-dsl" % "0.21.20" % Test)
