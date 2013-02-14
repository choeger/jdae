name := "jdae"

organization := "de.tuberlin.uebb"

version := "0.0.1"

scalaVersion := "2.10.0"

resolvers += "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

// Only needed to track snapshot releases, SBT automatically includes the releases repository.
resolvers += "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"

libraryDependencies ++= Seq(
		    "org.scalatest" %% "scalatest" % "1.8" % "test",
		    "org.scala-tools.testing" %% "scalacheck" % "1.9",
		    "com.google.guava" % "guava" % "14.0-rc1",
		    "com.google.code.findbugs" % "jsr305" % "1.3.+", //for javax.annotation.Nullable :(
		    "org.hamcrest" % "hamcrest-integration" % "1.3.RC2",
		    "com.novocode" % "junit-interface" % "0.8" % "test",
		    "org.ow2.asm" % "asm-all" % "4.0",
		    "net.sf.jgrapht" % "jgrapht" % "0.8.3",
		    "org.apache.commons" % "commons-math3" % "3.0",
		    "net.sf.trove4j" % "trove4j" % "3.0.3",
		    "net.sourceforge.jmatio" % "jmatio" % "1.0",
		    "junit" % "junit" % "4.10"
		    )

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")