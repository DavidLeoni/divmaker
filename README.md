<p class="josman-to-strip" align="center">
<img alt="DiverCLI" src="docs/img/diversicon-core-writing-100px.png" >
<br/>
</p>

# DIVMAKER


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

#### Credits

Design:

* Fiona McNeill - Heriot-Watt University, Edinburgh - f.mcneill at hw.ac.uk 
* Gabor Bella - DISI, University of Trento -  gabor.bella at unitn.it
* David Leoni - Heriot Watt University, Edinburgh - david.leoni at unitn.it

Programming:  

* David Leoni - Heriot Watt University, Edinburgh - david.leoni at unitn.it

Based on <a href="http://dkpro.github.io/dkpro-uby/" target="blank">DKPRO UBY framework</a>, by UKP Lab, Technische Universität Darmstadt.

Made possible thanks to:

&emsp;&emsp;&emsp;<a href="https://www.hw.ac.uk/schools/mathematical-computer-sciences/departments/computer-science.htm" target="_blank"> <img src="docs/img/hw.webp" width="80px" style="vertical-align:middle;"> </a> &emsp;&emsp;&emsp;<a href="https://www.hw.ac.uk/schools/mathematical-computer-sciences/departments/computer-science.htm" target="_blank"> Heriot-Watt Computer Science Department </a>  

&emsp;<a href="http://kidf.eu" target="_blank"> <img style="vertical-align:middle;" width="140px" src="docs/img/kidf-scientia.png"> </a> &emsp; <a href="http://kidf.eu" target="_blank"> Knowledge in Diversity Foundation </a> <br/>
