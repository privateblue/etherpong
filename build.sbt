organization := "privateblue"

name := "etherpong"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1"
)

assemblyJarName in assembly := s"${name.value}.jar"

enablePlugins(ScalaJSPlugin)
