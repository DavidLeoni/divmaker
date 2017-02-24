<p class="josman-to-strip">
WARNING: WORK IN PROGRESS - THIS IS ONLY A TEMPLATE FOR THE DOCUMENTATION. <br/>
RELEASE DOCS ARE ON THE <a href="http://diversicon-kb.eu/manual/divmaker" target="_blank">PROJECT WEBSITE</a>
</p>

This release allows to convert Princeton Wordnet 3.1 to LMF XML. With some minor tweak in the code, in theory you could convert to LMF also other databases in Princeton Wordnet format.  
<!--If you are upgrading from previous version, see [Release notes](CHANGES.md).-->

### Getting started

**With Maven**: If you use Maven as build system, put this in the `dependencies` section of your `pom.xml`:

```xml
    <dependency>
        <groupId>eu.kidf</groupId>
        <artifactId>divmaker</artifactId>
        <version>${project.version}</version>
    </dependency>
```

**Without Maven**: you can download DivMaker jar and its dependencies <a href="../releases/download/divmaker-#{version}/divmaker-${project.version}.zip" target="_blank"> from here</a>, then copy the jars to your project classpath.


In case updates are available, version numbers follow <a href="http://semver.org/" target="_blank">semantic versioning</a> rules.


### Create Wordnet 3.1

Worked on opendata server to create `dumps/diversicon/div-wn31.xml` issuing:

```bash
MAVEN_OPTS="-Xms1g -Xmx3g -XX:-UseGCOverheadLimit" mvn clean install -U  exec:java

```

### Testing

To execute a single test:

```bash
MAVEN_OPTS="-Xmx1024M -Xss128M -XX:MaxPermSize=124M -XX:+CMSClassUnloadingEnabled" mvn  -Dtest=DivUtilsIT#test
```
