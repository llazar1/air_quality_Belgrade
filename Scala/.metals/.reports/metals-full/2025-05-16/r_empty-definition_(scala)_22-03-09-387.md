error id: file:///D:/dev/workspaces/eclipse-workspace-2022/PDS-Spark/Scala/src/main/scala/InteraktivniUpiti.scala:
file:///D:/dev/workspaces/eclipse-workspace-2022/PDS-Spark/Scala/src/main/scala/InteraktivniUpiti.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 417
uri: file:///D:/dev/workspaces/eclipse-workspace-2022/PDS-Spark/Scala/src/main/scala/InteraktivniUpiti.scala
text:
```scala
import org.apache.spark.sql.SparkSession

object InteraktivniUpiti extends App { 

	val spark = SparkSession.builder()
  		.appName("Uvodni upiti")
		.master("local[*]")
  		.getOrCreate()

	val sc = spark.sparkContext 
	// 
	// Izvršiti u Spark shell-u ! Ne treba da se unosi gornji kod, već od ovog mesta nadole!!!
	//
    
    // Interaktivni shell:

    // Startovanje iz command prompta sa: > spa@@rk-shell.cmd

    // Rad sa RDD-om

    val textFile = spark.sparkContext.textFile("../dataset/notes.txt")

    textFile.count()

    textFile.first()	

    val linesWithJava = textFile.filter(line => line.contains("Java"))

    val linesWithJavaCount = textFile.filter(line => line.contains("Java")).count()


    textFile.map(line => line.split(" ").size).reduce((a, b) => if (a > b) a else b) 


    textFile.map(line => line.split(" ").size).reduce((a, b) => Math.max(a, b))


    // --------------
    // Rad sa Datasetom 
    // Ne treba sledeca linija da se unosi u Spark-shell, jer je uneta vec. 
    // U Metals VS Plug-inu mora, jer bez njeprijavljuje gresku
    import spark.implicits._ 

    val textFileDS =  spark.read.textFile("../dataset/notes.txt")
    val wordCounts = textFileDS.flatMap(line => line.split(" ")).groupByKey(identity).count()


    val javacount = wordCounts.filter(_._1 == "Java").show()
    val words = wordCounts.filter(_._2 == 3).show()

    val words2 = wordCounts.filter(w => w._1 =="Java" || w._2 == 2).show()
    
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: 