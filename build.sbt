name := "jdae"

organization := "de.tuberlin.uebb"

version := "0.1.0"

scalaVersion := "2.10.0"

javacOptions ++= Seq("-source", "1.7")

resolvers += "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

// Only needed to track snapshot releases, SBT automatically includes the releases repository.
resolvers += "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"

libraryDependencies ++= Seq(
		    "com.google.guava" % "guava" % "14.0-rc1",
		    "org.hamcrest" % "hamcrest-all" % "1.3",
		    "org.apache.commons" % "commons-math3" % "3.1.1",
		    "junit" % "junit" % "4.10",
		  	"com.google.code.gson" % "gson" % "2.2.2",
		     "com.novocode" % "junit-interface" % "0.10-M4" % "test"
		    )

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

EclipseKeys.withSource := true

pomExtra :=
<properties>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
<build>
  <plugins>
    <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-compiler-plugin</artifactId>
       <version>3.1</version>
       <configuration>
          <source>1.7</source>
          <target>1.7</target>
       </configuration>
    </plugin>
	<plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-pmd-plugin</artifactId>
      <version>3.0.1</version>
    </plugin>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>findbugs-maven-plugin</artifactId>
      <version>2.5.2</version>
    </plugin>
  </plugins>
</build>
<licenses>
  <license>
    <name>LGPL (GNU Lesser General Public License)</name>
    <url>http://www.gnu.org/licenses/lgpl.html</url>
    <distribution>repo</distribution>
  </license>
</licenses>