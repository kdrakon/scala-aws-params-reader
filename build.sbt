
name := "scala-aws-param-reader"
version := "0.0.1"
scalaVersion := "2.12.1"
crossScalaVersions := Seq("2.11.8", "2.12.1")
organization := "io.policarp"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-ssm" % "1.11.109"
)

scalacOptions in ThisBuild ++=  Seq(
  "-deprecation",
  "-unchecked",
  "-Xfatal-warnings",
  "-language:implicitConversions"
)

