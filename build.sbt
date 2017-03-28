import com.typesafe.sbt.SbtScalariform.ScalariformKeys

import scalariform.formatter.preferences.AlignParameters

name := "scala-aws-param-reader"
version := "0.0.1"
scalaVersion := "2.12.1"
crossScalaVersions := Seq("2.11.8", "2.12.1")
organization := "io.policarp"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-ssm" % "1.11.109"
)

SbtScalariform.scalariformSettings
ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, true)

scalacOptions in ThisBuild ++=  Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-language:implicitConversions"
)

