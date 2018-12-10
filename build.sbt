homepage := Some(url("https://github.com/miguno/akka-mock-scheduler"))

licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html"))

organization := "com.miguno.akka"

name := "akka-mock-scheduler"

resolvers ++= Seq(
  "typesafe-repository" at "http://repo.typesafe.com/typesafe/releases/"
)


// -------------------------------------------------------------------------------------------------------------------
// Variables
// -------------------------------------------------------------------------------------------------------------------

val akkaVersion = "2.5.19"
val javaVersion = "1.8"
val mainScalaVersion = "2.12.8"


// -------------------------------------------------------------------------------------------------------------------
// Dependencies
// -------------------------------------------------------------------------------------------------------------------

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test"
)


// ---------------------------------------------------------------------------------------------------------------------
// Compiler settings
// ---------------------------------------------------------------------------------------------------------------------

crossScalaVersions := Seq(mainScalaVersion, "2.11.12")

scalaVersion := mainScalaVersion

javacOptions in Compile ++= Seq(
  "-source", javaVersion,
  "-target", javaVersion,
  "-Xlint:unchecked",
  "-Xlint:deprecation")

scalacOptions ++= Seq(
  "-target:jvm-" + javaVersion,
  "-encoding", "UTF-8"
)

scalacOptions in Compile ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature",  // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code",
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

scalacOptions in Test ~= { (options: Seq[String]) =>
  options.filterNot(_ == "-Ywarn-value-discard").filterNot(_ == "-Ywarn-dead-code" /* to fix warnings due to Mockito */)
}


// ---------------------------------------------------------------------------------------------------------------------
// Sonatype settings
// ---------------------------------------------------------------------------------------------------------------------

// https://github.com/jodersky/sbt-gpg
credentials += Credentials(
  "GnuPG Key ID",
  "gpg",
  "5724DC6AAEC6D526992C234D07D42B2CB4799D71", // key identifier
  "ignored" // this field is ignored; passwords are supplied by pinentry
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <scm>
    <connection>scm:git:git@github.com:miguno/akka-mock-scheduler.git</connection>
    <developerConnection>scm:git:git@github.com:miguno/akka-mock-scheduler.git</developerConnection>
    <url>git@github.com:miguno/akka-mock-scheduler.git</url>
  </scm>
  <developers>
    <developer>
      <id>miguno</id>
      <name>Michael G. Noll</name>
      <url>http://www.michael-noll.com/</url>
    </developer>
  </developers>)


// ---------------------------------------------------------------------------------------------------------------------
// Testing settings
// ---------------------------------------------------------------------------------------------------------------------

// Write test results to file in JUnit XML format
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports/junitxml")

// Write test results to console.
//
// Tip: If you need to troubleshoot test runs, it helps to use the following reporting setup for ScalaTest.
//      Notably these suggested settings will ensure that all test output is written sequentially so that it is easier
//      to understand sequences of events, particularly cause and effect.
//      (cf. http://www.scalatest.org/user_guide/using_the_runner, section "Configuring reporters")
//
//        testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oUDT", "-eUDT")
//
//        // This variant also disables ANSI color output in the terminal, which is helpful if you want to capture the
//        // test output to file and then run grep/awk/sed/etc. on it.
//        testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oWUDT", "-eWUDT")
//
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-o")
