enablePlugins(SemanticdbPlugin)

name := "Raf-Spark-Scala-App"

version := "0.1"

scalaVersion := "2.13.13"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.5"

fork := true
javaOptions ++= Seq(
   "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
   "--add-opens=java.base/java.nio=ALL-UNNAMED",
   "--add-opens=java.base/java.lang=ALL-UNNAMED",
   "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
   "--add-opens=java.base/java.util=ALL-UNNAMED"
)

ThisBuild / semanticdbVersion := "4.8.11"
ThisBuild / semanticdbOptions += "-P:semanticdb:synthetics:on"
