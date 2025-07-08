error id: file:///D:/dev/workspaces/eclipse-workspace-2022/PDS-Spark/Scala/build.sbt:
file:///D:/dev/workspaces/eclipse-workspace-2022/PDS-Spark/Scala/build.sbt
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 63
uri: file:///D:/dev/workspaces/eclipse-workspace-2022/PDS-Spark/Scala/build.sbt
text:
```scala
enablePlugins(SemanticdbPlugin)

name := "Raf-Spark-Scala-App@@"

version := "0.1"

scalaVersion := "2.13.13"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.5"

fork := true
javaOptions ++= Seq(
  "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
  "--add-opens=java.base/java.nio=ALL-UNNAMED",
  "--add-opens=java.base/java.lang=ALL-UNNAMED"
)

ThisBuild / semanticdbVersion := "4.8.11"
ThisBuild / semanticdbOptions += "-P:semanticdb:synthetics:on"

```


#### Short summary: 

empty definition using pc, found symbol in pc: 