error id: file:///D:/dev/workspaces/eclipse-workspace-2022/PDS-Spark/Scala/src/main/scala/Proba.scala:
file:///D:/dev/workspaces/eclipse-workspace-2022/PDS-Spark/Scala/src/main/scala/Proba.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 87
uri: file:///D:/dev/workspaces/eclipse-workspace-2022/PDS-Spark/Scala/src/main/scala/Proba.scala
text:
```scala
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types._


i@@mport spark.implicits._


object Proba extends App {
  println("Hello, RAF SPARK!")

  val schema = StructType(Seq(StructField("Ime", StringType, nullable = false), StructField("GodinaRodjenja", IntegerType, nullable = false), StructField("ZavrsenFakultet", StringType, nullable = false), StructField("Sektor", StringType, nullable = false), StructField("Plata", DoubleType, nullable = false)))

  // Kreiramo podatke (redove)
  val podaci = Seq(Row("Ana Anic", 1995, "RAF", "Marketing", 1200.0), Row("Marko Maric", 1990, "RAF", "IT", 2500.0), Row("Jelena Jelic", 1992, "FON", "Marketing", 1000.0),Row("Pera Peric", 1995, "ETF", "IT", 1800.0), Row("Mika Milic", 1995, "RAF", "IT", 1700.0))

  // Kreiramo DataFrame
  val df = spark.createDataFrame(spark.sparkContext.parallelize(podaci), schema)


  

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 