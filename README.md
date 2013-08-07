jdae
====

A DAE solving library written in Java

This lib is currently under active development, so expect API changes. 
See [examples](src/main/java/de/tuberlin/uebb/jdae/examples) for some introductionary models.

Compiling and Testing
---------------------

jdae is build using sbt. To compile the lib, simply use:

```
sbt compile
```

Unfortunately the junit-interface does not yet allow for Junit 4.11 tests, 
so to test you have to use maven. To do so, issue the following commands:

```
sbt make-pom
cp target/jdae-0.1.0.pom pom.xml
mvn test

```
