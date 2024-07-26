val scala3Version = "3.4.2"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "scala-webrtc-chat",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalacOptions += "-Wnonunit-statement",

    // Tell Scala.js that this is an application with a main method
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    libraryDependencies += "org.typelevel" %%% "cats-core" % "2.12.0",
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.5.4",
    libraryDependencies += "co.fs2" %%% "fs2-core" % "3.10.2",
    libraryDependencies += "co.fs2" %%% "fs2-scodec" % "3.10.2",
    libraryDependencies += "co.fs2" %%% "fs2-io" % "3.10.2",
    libraryDependencies += "com.armanbilge" %%% "calico" % "0.2.2"
  )

lazy val buildDocs = taskKey[Unit]("Build the output to the docs directory")

buildDocs := ({
  (Compile / compile).value

  // Copy HTML from resources to docs
  val html = (baseDirectory.value / "src" / "main" / "resources" / "index.html")
  val docs = (baseDirectory.value / "docs" / "index.html")
  IO.copyFile(html, docs)

  // Copy JS and JS.map from target to docs
  val js = (target.value / s"scala-$scala3Version" / "scala-webrtc-chat-opt" / "main.js")
  val jsMap = (target.value / s"scala-$scala3Version" / "scala-webrtc-chat-opt" / "main.js.map")
  val docsJS = (baseDirectory.value / "docs" / "main.js")
  val docsJSMap = (baseDirectory.value / "docs" / "main.js.map")
  IO.copyFile(js, docsJS)
  IO.copyFile(jsMap, docsJSMap)
})
