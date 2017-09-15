organization := "privateblue"

name := "etherpong"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.0.0-MF",
  "org.scala-js" %%% "scalajs-dom" % "0.9.1"
)

assemblyJarName in assembly := s"${name.value}.jar"

enablePlugins(DockerPlugin)

enablePlugins(ScalaJSPlugin)

dockerfile in docker := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("java")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}

imageNames in docker := Seq(
  ImageName(s"${organization.value}/${name.value}:latest")
)
