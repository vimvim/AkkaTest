name := """hello-akka"""

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.1",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "junit" % "junit" % "4.11" % "test",
  "com.novocode" % "junit-interface" % "0.10" % "test"
  // "org.jcodec" % "jcodec" % "0.1.8"
)

resolvers += "JMXFLib resolver" at "http://videogorillas.com/m2/public"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")