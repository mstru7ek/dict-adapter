
ThisBuild / scalaVersion := "2.13.3"

lazy val dictCore = (project in file("dict-adapter-core"))
  .settings(
    name := "dict-adapter-core",
    version := "0.1",
    libraryDependencies :=
      Seq(
        guice,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
        "org.apache.httpcomponents" % "httpclient" % "4.5.6",
        "org.jsoup" % "jsoup" % "1.11.3",
        "com.jayway.jsonpath" % "json-path" % "2.4.0",
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.0",
        "org.eclipse.jetty.aggregate" % "jetty-all" % "9.4.12.v20180830",
        "io.spray" %% "spray-json" % "1.3.5"
      )
  )




