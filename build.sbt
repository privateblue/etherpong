organization := "privateblue"

name := "etherpong"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1"
)

enablePlugins(ScalaJSPlugin)

enablePlugins(ScalaJSBundlerPlugin)

webpackBundlingMode := BundlingMode.LibraryAndApplication()

emitSourceMaps := false

scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }

npmDependencies in Compile ++= Seq(
  "web3" -> "0.20.1"
)
