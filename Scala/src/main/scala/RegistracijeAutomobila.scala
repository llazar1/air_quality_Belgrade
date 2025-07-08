import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._



object RegistracijeAutomobila extends App { 

	val spark = SparkSession.builder()
  		.appName("Registracije Automobila")
		.master("local[*]")
  		.getOrCreate()
	
	val sc = spark.sparkContext
	sc.setLogLevel("WARN")
	
	import spark.implicits._ 

	/* 
		Dataset - Podaci o registrovanim automobilima u američkoj državi Vašington 
		https://www.kaggle.com/datasets/adityakishor1/electric-vehicle-population-data-2024
		Fajl sa podacima se nalazi u dataset folderu unutar projekta!

Kolone:
|VIN (1-10)|County|City|State|Postal Code|Model Year|Make|Model|Electric Vehicle Type|Clean Alternative Fuel Vehicle (CAFV) Eligibility|Electric Range|Base MSRP|Legislative District|DOL Vehicle ID|Vehicle Location|Electric Utility|2020 Census Tract|

Deo dataset-a:
+----------+---------+-----------------+-----+-----------+----------+----------+-------+---------------------+-------------------------------------------------+--------------+---------+--------------------+--------------+--------------------+--------------------+-----------------+
|VIN (1-10)|   County|             City|State|Postal Code|Model Year|      Make|  Model|Electric Vehicle Type|Clean Alternative Fuel Vehicle (CAFV) Eligibility|Electric Range|Base MSRP|Legislative District|DOL Vehicle ID|    Vehicle Location|    Electric Utility|2020 Census Tract|
+----------+---------+-----------------+-----+-----------+----------+----------+-------+---------------------+-------------------------------------------------+--------------+---------+--------------------+--------------+--------------------+--------------------+-----------------+
|WBY8P6C58K|     King|          Seattle|   WA|      98115|      2019|       BMW|     I3| Battery Electric ...|                             Clean Alternative...|           153|        0|                  43|     259254397|POINT (-122.30082...|CITY OF SEATTLE -...|      53033003601|
|5YJSA1DN4D|   Kitsap|        Bremerton|   WA|      98312|      2013|     TESLA|MODEL S| Battery Electric ...|                             Clean Alternative...|           208|    69900|                  35|     127420940|POINT (-122.69612...|PUGET SOUND ENERG...|      53035080700|
|5YJSA1E26J|     King|             Kent|   WA|      98042|      2018|     TESLA|MODEL S| Battery Electric ...|                             Clean Alternative...|           249|        0|                  47|     170287183|POINT (-122.11451...|PUGET SOUND ENERG...|      53033031708|
|WBY2Z2C54E|     King|         Bellevue|   WA|      98004|      2014|       BMW|     I8| Plug-in Hybrid El...|                             Not eligible due ...|            14|        0|                  41|     205545868|POINT (-122.20239...|PUGET SOUND ENERG...|      53033024002|
|5YJXCDE23J|     King|         Bellevue|   WA|      98004|      2018|     TESLA|MODEL X| Battery Electric ...|                             Clean Alternative...|           238|        0|                  41|     237977386|POINT (-122.20239...|PUGET SOUND ENERG...|      53033023601|
|WBY33AW0XP|     King|          Seattle|   WA|      98109|      2023|       BMW|     I4| Battery Electric ...|                             Eligibility unkno...|             0|        0|                  36|     238283545|POINT (-122.34415...|CITY OF SEATTLE -...|      53033007002|
...
*/


	// 
	// Kada se izvršava u Spark shell-u, ne treba da se unosi gornji kod, već od ovog mesta nadole!!!
	//

	//  Zadatak br. 1 - U kom gradu se najviše vozi BMW ?

	// RDD Rešenje:
	// -------

	val automobili = sc.textFile("../dataset/Electric_Vehicle_Population_data.csv")
	
	val bmwRows = automobili.filter(line => line.contains("BMW")).map(_.split(",")).map(attributes => (attributes(2), 1))
	val bmwCitiesRDD = bmwRows.reduceByKey(_ + _).sortBy(_._2, ascending = false)
	// Alternativno može da se napise:
	// val bmwCitiesRDD = bmwRows.reduceByKey((a,b) => a + b).sortBy(par => par._2, ascending = false)
	// ili val bmwCitiesRDD = bmwRows.reduceByKey((a,b) => a + b).sortBy({case (grad, broj) => broj}, ascending = false)
	
	println("\nZADATAK 1 - RDD - MAP_REDUCE RESENJE")
	bmwCitiesRDD.take(5).foreach(println)

	/* ZADATAK 1 - RDD - MAP_REDUCE Rezultat
		(1671,Seattle)
		(517,Bellevue)
		(319,Redmond)
		(315,Kirkland)
		(305,Sammamish)
	*/

	// DataFrame rešenje:
	// -------------
	
	val autoDS = spark.read.option("header", "true").option("inferSchema","true").csv("../dataset/Electric_Vehicle_Population_data.csv")
	
	val bmwCities = autoDS.filter($"Make" === "BMW")
	val result = bmwCities.groupBy("City").
							agg(count("*") as "Count")
							.orderBy($"Count".desc)

	// Za kopiranje u Spark-Shell:
	// val result = autoDS.filter($"Make" === "BMW").groupBy("City").agg(count("*") as "Count").orderBy($"Count".desc)
	
	println("\nZADATAK 1 - Dataframe RESENJE")
	result.show()
	
	/* ZADATAK 1 - Rezultat: 
	+-----------------+-----+
	|             City|Count|
	+-----------------+-----+
	|          Seattle| 1671|
	|         Bellevue|  517|
	|          Redmond|  319|
	|         Kirkland|  315|
	|        Sammamish|  305|
		....
	*/
	
	// Zadatak br. 2 - U kojoj oblasti se voze najmladji automobili u proseku, a u kojoj se voze najstariji automobili
	
	val countiesYear = autoDS.select($"County", $"Model Year")
							.withColumn("Age", $"Model Year")
	
	val countiesCarAvgYear = countiesYear.groupBy("County").
										agg(avg($"Model Year") as "AvgAge")
										.orderBy($"AvgAge".desc)

	// Za kopiranje u Spark-Shell:
	// val countiesYear = autoDS.select($"County", $"Model Year").withColumn("Age", $"Model Year")
	// val countiesCarAvgYear = countiesYear.groupBy("County").agg(avg($"Model Year") as "AvgAge").orderBy($"AvgAge".desc)
	
	println("ZADATAK 2 - REZULTAT")
	println("		 [Okrug, godiste]")

	println("Najmladji	:"+countiesCarAvgYear.head(1).mkString(", "))	// Može i countiesCarAvgYear.first().mkString(", "))
	println("Najstariji	:"+countiesCarAvgYear.tail(1).mkString(", "))
	
	/* ZADATAK 2 - REZULTAT
	Najmladji  :[Lake,2023.1666666666667]
	Najstariji :[Charles,2012.0]
	*/

	// -----------
	// Zadatak br. 3 - U kojem okrugu Tesla Model 3 je najpopularniji ?
	
	val grouppedDF = autoDS.groupBy("County")
							.agg(count("*") as "Total Cars", sum(when($"Model" === "MODEL 3", 1).otherwise(0)) as "Tesla Model 3 total")
							.filter($"Total Cars">10)
	
	val percentageDF = grouppedDF.withColumn("Procenat Tesla Model 3", ($"Tesla Model 3 Total"/$"Total Cars")*100)
						.orderBy($"Procenat Tesla Model 3".desc)

	// Za kopiranje u Spark-Shell:
	// val grouppedDF = autoDS.groupBy("County").agg(count("*") as "Total Cars", sum(when($"Model" === "MODEL 3", 1).otherwise(0)) as "Tesla Model 3 total").filter($"Total Cars">10)				
	// val percentageDF = grouppedDF.withColumn("Procenat Tesla Model 3", ($"Tesla Model 3 Total"/$"Total Cars")*100).orderBy($"Procenat Tesla Model 3".desc)
	
	println("\nZADATAK 3 - REZULTAT")
	percentageDF.show(50)

	/* ZADATAK 3 - REZULTAT 
	+------------+----------+-------------------+----------------------+
	|      County|Total Cars|Tesla Model 3 total|Procenat Tesla Model 3|
	+------------+----------+-------------------+----------------------+
	|   San Diego|        24|                  7|    29.166666666666668|
	|    Franklin|       617|                138|    22.366288492706644|
	|       Ferry|        32|                  7|                21.875|
	|      Yakima|      1070|                212|    19.813084112149532|
	|      Benton|      2282|                406|    17.791411042944784|
	|   Snohomish|     22086|               3928|    17.785022186000184|
	...
	*/
	
	
	// ZADATAK 4 - Koji su najmladji automobili koji se voze:
	
	import java.time.Year
	val year = Year.now.getValue
	
	val carModelYearDS = autoDS.select("Make", "Model", "Model Year")
	val carModelAvgYear = carModelYearDS.groupBy("Make", "Model")
										.agg(count("*") as "Total", 
											avg(negative($"Model Year")+Year.now.getValue) as "Avg Age")
										.orderBy($"Avg Age".asc)
	
	// Za kopiranje u Spark-Shell:
	// val carModelYearDS = autoDS.select("Make", "Model", "Model Year")
	// val carModelAvgYear = carModelYearDS.groupBy("Make", "Model").agg(count("*") as "Total", avg(negative($"Model Year")+Year.now.getValue) as "Avg Age").orderBy($"Avg Age".asc)

	println("ZADATAK 4 - REZULTAT")
	carModelAvgYear.show(50)

	/* ZADATAK 4 - REZULTAT 
	+-------------+----------------+-----+------------------+
	|         Make|           Model|Total|           Avg Age|
	+-------------+----------------+-----+------------------+
	|          BMW|              I5|   79|               1.0|
	|    CHEVROLET|       BLAZER EV|   34|               1.0|
	|        MAZDA|           CX-90|  545|               1.0|
	|         AUDI|              Q8|  338|               1.0|
	|        TESLA|      CYBERTRUCK|   65|               1.0|
	|          BMW|              XM|   10|               1.0|
	|        DODGE|          HORNET|  614|               1.0|
	|   ALFA ROMEO|          TONALE|   47|               1.0|
	|        LEXUS|              TX|    4|               1.0|
	|         AUDI|             SQ8|    9|               1.0|
	|          KIA|             EV9|  320|               1.0|
	|    CHEVROLET|    SILVERADO EV|   52|               1.0|
	|          GMC|HUMMER EV PICKUP|    4|               1.0|
	|        LEXUS|              RX|   65|               1.0|
	|          BMW|            750E|    1|               1.0|
	|  ROLLS ROYCE|         SPECTRE|    2|               1.0|
	|     CADILLAC|           LYRIQ|  405|1.1407407407407408|
	|      GENESIS|            GV70|   38|1.4210526315789473|
	|          BMW|              I7|   39|1.5128205128205128|
	|        LEXUS|              NX|  223|1.5650224215246638|
	...
	*/
}