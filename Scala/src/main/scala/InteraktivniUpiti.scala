import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

object InteraktivniUpiti extends App { 

	val spark = SparkSession.builder()
  		.appName("Interaktivni Upiti")
		.master("local[*]")
  		.getOrCreate()

	val sc = spark.sparkContext
    sc.setLogLevel("WARN")
	// 
	// Izvršiti u Spark shell-u ! Ne treba da se unosi gornji kod, već od ovog mesta nadole!!!
	//
    
    // Interaktivni shell:

    // Startovanje iz command prompta sa: > spark-shell.cmd

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
    
    // Primeri za Window funkcije

    // Definišemo šemu
    val schema = StructType(Seq(StructField("Ime", StringType, nullable = false), StructField("GodinaRodjenja", IntegerType, nullable = false), StructField("ZavrsenFakultet", StringType, nullable = false), StructField("Sektor", StringType, nullable = false), StructField("Plata", DoubleType, nullable = false)))

    // Kreiramo podatke (redove)
    val podaci = Seq(Row("Ana Anic", 1995, "RAF", "Marketing", 1200.0), Row("Marko Maric", 1990, "RAF", "IT", 2500.0), Row("Jelena Jelic", 1992, "FON", "Marketing", 1000.0),Row("Pera Peric", 1995, "ETF", "IT", 1800.0), Row("Mika Milic", 1995, "RAF", "IT", 1700.0))

    // Kreiramo DataFrame
    val df = spark.createDataFrame(spark.sparkContext.parallelize(podaci), schema)

    //Odrediti za svakog zaposlenog koji je po redu po plati u svom sektoru?
    df.withColumn("RangPlate", rank().over(Window.partitionBy("Sektor").orderBy($"Plata".desc))).show()


    // Odrediti broj ljudi u sektoru sa istim godištem.
    df.withColumn("IstoGodiste", count("*").over(Window.partitionBy("Sektor", "GodinaRodjenja"))).show()


    //Odrediti prosek plate ljudi sa završenim istim fakultetom.
    df.withColumn("ProsekPlateFakultet", avg("Plata").over(Window.partitionBy("ZavrsenFakultet"))).show()


    //Odrediti razliku u plati do prvog sledećeg po plati u sektoru.
    df.withColumn("RazlikaPlate", coalesce(negate($"Plata") + lead("Plata", 1).over(Window.partitionBy("Sektor").orderBy("Plata")), lit(0))).show()

    // Join operatori

    val studentiSchema = StructType(Seq(StructField("Ime", StringType, nullable = false), StructField("SifIndeksa", StringType, nullable = false), StructField("Smer", StringType, nullable = false), StructField("Godina", IntegerType, nullable = false)))
    val studentiPodaci = Seq(Row("Ana Anic", "100/95", "RI", 4), Row("Marko Maric", "150/96", "RN", 3), Row("Jelena Jelic", "220/96", "RI", 3), Row("Pera Peric", "64/94", "RI", 4), Row("Mika Milic", "21/96", "RN", 3))
    val studentiDF = spark.createDataFrame(spark.sparkContext.parallelize(studentiPodaci), studentiSchema)

    val izborniPredmetiSchema = StructType(Seq(StructField("PredmetId", IntegerType, nullable = false), StructField("Naziv_Predmeta", StringType, nullable = false), StructField("SifIndeksa", StringType, nullable = false)))
    val izborniPredmetiPodaci = Seq(Row(1, "Primenjeni DS", "100/95"), Row(1, "Primenjeni DS", "64/94"), Row(1, "Primenjeni DS", "21/96"), Row(2, "Napredne DBMS", "150/96"), Row(2, "Napredne DBMS", "21/96"), Row(3, "Internet Stvari", ""))
    val predmetiDF = spark.createDataFrame(spark.sparkContext.parallelize(izborniPredmetiPodaci), izborniPredmetiSchema)

    studentiDF.join(predmetiDF, "SifIndeksa", "left").show()
    // U situaciji ako se radi join po kolonama koje se ne zovu isto u oba Dataframe-a, 
    // recimo ako bi se kolona u predmetiDF zvala Student_ID, onda bi upit sa join-om glasio: 
   // studentiDF.join(predmetiDF, studentiDF("SifIndeksa") === predmetiDF("Student_Id")).show()

    studentiDF.join(predmetiDF, "SifIndeksa", "right").show()

    studentiDF.join(predmetiDF, "SifIndeksa", "full_outer").show()

}