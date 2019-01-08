# JannyRecipes

Spiritual successor of [CustomRecipes](https://github.com/Jannyboy11/CustomRecipes) implemented in Scala!
Currently compiles and loads, but lacks features!

## Compiling

You can use either [Scala SBT](https://www.scala-sbt.org/) or [Apache Maven](https://maven.apache.org/).
- Be sure to have [JDK 11](http://openjdk.java.net/install/) or higher installed.
- Be sure to have run [BuildTools](https://www.spigotmc.org/wiki/buildtools/) with `java -jar BuildTools.jar --rev 1.13.2`.

Then take your pick:

#### SBT
`sbt assembly`

#### Maven
`mvn package`

## Deploying
Drop [ScalaLoader](https://www.spigotmc.org/resources/scalaloader.59568/) into your <server_root>/plugins folder,
then drop JannyRecipes into your <server_root>/plugins/ScalaLoader/scalaplugins folder

## License
This software is licensed under the LGPLv3. It's included in the LICENSE.txt file.
Since the LGPL specifies additional sets of permissions on top of the GPL, I included a copy of the GPL as well.


