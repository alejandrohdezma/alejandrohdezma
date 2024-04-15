ThisBuild / scalaVersion := "2.13.13"
ThisBuild / organization := "com.alejandrohdezma"

addCommandAlias("ci-test", "fix --check; test")

ThisBuild / excludedFiles += "docs/**"
ThisBuild / excludedFiles += "**/*release*"

lazy val `munit` = module
  .settings(libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test)
  .settings(libraryDependencies += "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test)
  .settings(libraryDependencies += "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test)
  .settings(libraryDependencies += "com.alejandrohdezma" %% "http4s-munit" % "0.15.1" % Test)
  .settings(libraryDependencies += "com.dimafeng" %% "testcontainers-scala-munit" % "0.40.14" % Test)
  .settings(libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.5" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-async-http-client" % "0.23.12" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-client" % "0.23.26" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-dsl" % "0.23.26" % Test)

lazy val site = module
  .enablePlugins(LaikaPlugin)
  .settings(laikaSite / target := file("docs"))
  .settings(laikaTheme := laika.theme.Theme.empty)
  .settings(laikaConfig := LaikaConfig.defaults)
  .settings(laikaConfig := laikaConfig.value.withRawContent)
  .settings(laikaConfig := laikaConfig.value.withConfigValue("livereload", !sys.env.contains("CI")))
  .settings(laikaExtensions += laika.markdown.github.GitHubFlavor)
  .settings(laikaExtensions += laika.parse.code.SyntaxHighlighting)
  .settings(laikaExtensions += BlogDirectives)
  .settings(laikaExtensions += HTMLRenderers)
  .settings(watchTriggers += baseDirectory.value.toGlob / "**" / "*.html")
  .settings(watchTriggers += baseDirectory.value.toGlob / "**" / "*.md")
  .settings(watchTriggers += baseDirectory.value.toGlob / "**" / "*.css")
  .settings(watchTriggers += baseDirectory.value.toGlob / "**" / "*.js")
