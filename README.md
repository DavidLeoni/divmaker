DIVMAKER


Quick and dirty Java command line app to generate lexicons.

GPL licensed. 


Note to myself:

Worked on opendata server to create `diversicon/div-wn31.xml` issuing:

```
MAVEN_OPTS="-Xms1g -Xmx3g -XX:-UseGCOverheadLimit" mvn clean install -U  exec:java

```

To execute a single test:

```
MAVEN_OPTS="-Xmx1024M -Xss128M -XX:MaxPermSize=124M -XX:+CMSClassUnloadingEnabled" mvn  -Dtest=DivUtilsIT#test
```

