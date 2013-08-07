name := "jdae"

organization := "de.tuberlin.uebb"

version := "0.1.0"

autoScalaLibrary := false

crossPaths := false

EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

EclipseKeys.withSource := true

javacOptions ++= Seq("-source", "1.7")


libraryDependencies ++= Seq(
		    "com.google.guava" % "guava" % "14.0.1",
		    "com.google.guava" % "guava-testlib" % "14.0.1",		    
		    "org.hamcrest" % "hamcrest-all" % "1.3",
		    "org.apache.commons" % "commons-math3" % "3.1.1",
		    "junit" % "junit" % "4.11",
		    "com.google.code.gson" % "gson" % "2.2.2",
		    "com.googlecode.efficient-java-matrix-library" % "ejml" % "0.23",
		    "net.sf.trove4j" % "trove4j" % "3.0.3",
		    "com.novocode" % "junit-interface" % "0.10" % "test"
		    )

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

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
      <groupId>org.jacoco</groupId>
      <artifactId>jacoco-maven-plugin</artifactId>
      <version>0.6.3.201306030806</version>
      <executions>		
        <execution>	
          <goals>
            <goal>prepare-agent</goal>
          </goals>
        </execution>
        <execution>
          <id>report</id>
          <phase>prepare-package</phase>
          <goals>
            <goal>report</goal>
          </goals>
        </execution>
      </executions>
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