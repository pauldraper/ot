lazy val common = crossProject
  .settings(
    libraryDependencies ++= Seq(
      "com.github.benhutchison" %%% "prickle" % "1.1.10",
      "com.lihaoyi" %%% "scalatags" % "0.5.4",
      "org.scalatest" %%% "scalatest" % "3.0.0-M10" % Test
    )
  )
lazy val commonJS = common.js.enablePlugins(ScalaJSPlay)
lazy val commonJVM = common.jvm

lazy val client = project
  .dependsOn(commonJS)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.0"
    ),
    persistLauncher := true
  )

lazy val server = project
  .aggregate(commonJS)
  .dependsOn(commonJVM)
  .disablePlugins(PlayLayoutPlugin)
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      "com.vmunier" %% "play-scalajs-scripts" % "0.4.0"
    ),
    pipelineStages := Seq(scalaJSProd),
    scalaJSProjects := Seq(client)
  )

emitSourceMaps in (ThisBuild, fullOptJS) := true

scalaJSUseRhino in ThisBuild := false

scalaVersion in ThisBuild := "2.11.7"
