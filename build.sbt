import sbt.url

name := "scala-aws-params-reader"
version := "0.1.0"
scalaVersion := "2.12.1"
crossScalaVersions := Seq("2.11.8", "2.12.1")
organization := "io.policarp"
homepage := Some(url("https://github.com/kdrakon/scala-aws-params-reader"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/kdrakon/scala-aws-params-reader"),
    "scm:git@github.com:kdrakon/scala-aws-params-reader.git"
  )
)

libraryDependencies ++= Seq(

  "com.amazonaws" % "aws-java-sdk-ssm" % "1.11.109",

  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
  "com.github.japgolly.nyaya" %% "nyaya-gen" % "0.8.1" % "test"
)

scalacOptions in ThisBuild ++=  Seq(
  "-deprecation",
  "-unchecked",
  "-Xfatal-warnings",
  "-language:implicitConversions"
)

pomIncludeRepository := { _ => false }
publishMavenStyle := true
licenses := Seq("Apache License 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
developers := List(
  Developer(
    id    = "kdrakon",
    name  = "Sean Policarpio",
    email = "",
    url   = url("http://policarp.io")
  )
)
useGpg := true
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

