Global / excludeLintKeys += webpackExtraArgs

val scala3Version = "3.4.2"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalablyTypedConverterPlugin)
  .settings(
    name         := "scala-webrtc-chat",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq("-Wnonunit-statement", "-feature", "-deprecation", "-unchecked"),

    // Tell Scala.js that this is an application with a main method
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    Compile / fullOptJS / webpackExtraArgs ++= Seq("--output-hash-function", "xxhash64"),
    webpack / version := "5.93.0",
    webpackCliVersion := "5.1.4",
    startWebpackDevServer / version := "5.0.4",

    // Scala.js dependencies
    libraryDependencies += "org.typelevel"  %%% "cats-core"   % "2.12.0",
    libraryDependencies += "org.typelevel"  %%% "cats-effect" % "3.5.4",
    libraryDependencies += "co.fs2"         %%% "fs2-core"    % "3.10.2",
    libraryDependencies += "co.fs2"         %%% "fs2-scodec"  % "3.10.2",
    libraryDependencies += "co.fs2"         %%% "fs2-io"      % "3.10.2",
    libraryDependencies += "com.armanbilge" %%% "calico"      % "0.2.2",

    // Javascript dependencies
    Compile / npmDependencies ++= Seq("peerjs" -> "1.5.4")
  )

lazy val buildDocs = taskKey[Unit]("Build the output to the docs directory")

buildDocs :=
  ({
    (Compile / fullOptJS / webpack).value

    // Copy HTML from resources to docs
    val html = (baseDirectory.value / "src" / "main" / "resources" / "index.html")
    val docs = (baseDirectory.value / "docs" / "index.html")
    IO.copyFile(html, docs)

    // Copy JS and JS.map from target to docs
    val js        = (target.value / s"scala-$scala3Version" / "scalajs-bundler" / "main" / "scala-webrtc-chat-opt-bundle.js")
    val jsMap     = (target.value / s"scala-$scala3Version" / "scalajs-bundler" / "main" / "scala-webrtc-chat-opt-bundle.js.map")
    val docsJS    = (baseDirectory.value / "docs" / "main.js")
    val docsJSMap = (baseDirectory.value / "docs" / "main.js.map")
    IO.copyFile(js, docsJS)
    IO.copyFile(jsMap, docsJSMap)
  })
