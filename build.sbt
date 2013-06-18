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
		    "org.hamcrest" % "hamcrest-integration" % "1.3.RC2",
		    "org.ow2.asm" % "asm-all" % "4.0",
		    "net.sf.jgrapht" % "jgrapht" % "0.8.3",
		    "org.apache.commons" % "commons-math3" % "3.1.1",
		    "net.sourceforge.jmatio" % "jmatio" % "1.0",
		    "junit" % "junit" % "4.10",
		  	"com.google.code.gson" % "gson" % "2.2.2",
		     "com.novocode" % "junit-interface" % "0.10-M2" % "test"
		    )

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

EclipseKeys.withSource := true

pomExtra :=
<build>
  <plugins>
    <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-compiler-plugin</artifactId>
       <configuration>
          <source>1.7</source>
          <target>1.7</target>
       </configuration>
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