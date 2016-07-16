DIVMAKER


Quick and dirty Java command line app to generate lexicons.

GPL licensed. 


Note to myself:

Worked on opendata server to create `target/wn30.xml` by issuing:

```

MAVEN_OPTS="-Xms1024M -Xmx3g -XX:-UseGCOverheadLimit" mvn test

```

To execute a single test:

```
MAVEN_OPTS="-Xmx1024M -Xss128M -XX:MaxPermSize=124M -XX:+CMSClassUnloadingEnabled" mvn  -Dtest=DivUtilsIT#testRetestRestoreNonAugmentedNonResourceUbyWordnetH2Sql
```

