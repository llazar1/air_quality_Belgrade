error id: file:///D:/dev/workspaces/eclipse-workspace-2022/PDS-Spark/Scala/src/main/scala/Walmart.scala:org/apache/spark/sql/Dataset#filter().
file:///D:/dev/workspaces/eclipse-workspace-2022/PDS-Spark/Scala/src/main/scala/Walmart.scala
empty definition using pc, found symbol in pc: 
found definition using semanticdb; symbol org/apache/spark/sql/Dataset#filter().
empty definition using fallback
non-local guesses:

offset: 6153
uri: file:///D:/dev/workspaces/eclipse-workspace-2022/PDS-Spark/Scala/src/main/scala/Walmart.scala
text:
```scala
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._


object Walmart extends App {

    val spark = SparkSession.builder()
  		.appName("Wallmart")
		.master("local[*]")
  		.getOrCreate()
	
	val sc = spark.sparkContext
    sc.setLogLevel("OFF")
	
    import spark.implicits._ 

    /* -------------------------
	 Kada se izvršava u Spark shell-u, ne treba da se unosi gornji kod, već od ovog mesta nadole!!!
	 Jednu naredbu u shell-u unosite u jednoj liniji, ovde je prelomljeno u vise linija, da bi bilo čitljivije!
     Primer kako treba je prva naredba sa učitavanjem val wallmartDS = spark.read.option("header", "true").option("inferSchema","true").csv("D:/Nastava/Distribuirani sistemi/Projekti/2024/Spark Projekat II/walmart.csv")

     -----------------------
    */
    
    /* 
     Dataset - Podaci o prodaji u maloprodajnim objektima kompanije Wallmart u SAD
     https://www.kaggle.com/datasets/devarajv88/walmart-sales-dataset
     Fajl sa podacima se nalazi u dataset folderu unutar projekta!

     Kolone:
User_ID|Product_ID|Gender|Age  |Occupation|City_Category|Stay_In_Current_City_Years|Marital_Status|Product_Category|Purchase|
     
     Deo dataset-a:
    +-------+----------+------+-----+----------+-------------+--------------------------+--------------+----------------+--------+
User_ID|Product_ID|Gender|  Age|Occupation|City_Category|Stay_In_Current_City_Years|Marital_Status|Product_Category|Purchase|
    +-------+----------+------+-----+----------+-------------+--------------------------+--------------+----------------+--------+
1000001| P00069042|     F| 0-17|        10|            A|                         2|             0|               3|    8370|
1000001| P00248942|     F| 0-17|        10|            A|                         2|             0|               1|   15200|
1000001| P00087842|     F| 0-17|        10|            A|                         2|             0|              12|    1422|
1000001| P00085442|     F| 0-17|        10|            A|                         2|             0|              12|    1057|
1000002| P00285442|     M|  55+|        16|            C|                        4+|             0|               8|    7969|
1000003| P00193542|     M|26-35|        15|            A|                         3|             0|               1|   15227|
    ...
    */

    val wallmartDS = spark.read.option("header", "true").option("inferSchema","true").csv("../dataset/walmart.csv")
   


    // Zadatak 1 - Којег су занимања самци који највише просечно троше новца по једној куповини у одређеној категорији града и који су то износи?

    val samciTrosenjeZanimanja = wallmartDS.filter($"Marital_Status" ===0)
                                    .groupBy("User_ID", "Occupation", "City_Category")
                                    .agg(avg($"Purchase") as "Prosecna Potrosnja")
                                    .orderBy($"Prosecna Potrosnja".desc)
    
    println("ZADATAK 1 - REZULTAT:")
    samciTrosenjeZanimanja.show(20)

    /* ZADATAK 1 - REZULTAT: 
User_ID|Occupation|City_Category|Prosecna Potrosnja|
    +-------+----------+-------------+------------------+
1003902|         5|            C|18577.893617021276|
1003461|         0|            C|           17508.7|
1000101|         3|            A|17246.439393939392|
1002983|         4|            C|16790.612903225807|
1004474|         7|            C|16639.941176470587|
    .....
    
    */

    // Zadatak 2 - Дати листу производа са информацијом у којој животној доби се највише купује дати производ и у којој категорији града.  

    import org.apache.spark.sql.expressions.Window

    val proizvodProdaja = wallmartDS.groupBy("Product_ID", "Age", "City_Category")
                                    .agg(count("*") as "Prodaja")

    val proizvodProdajaMaxSale = proizvodProdaja.withColumn("Max Prodaja", max("Prodaja").over(Window.partitionBy("Product_ID")))
                                                            .filter($"Prodaja" === $"Max Prodaja")
                                                            .orderBy($"Max Prodaja".desc)
                                                            .drop("Prodaja")

    println("ZADATAK 2 - REZULTAT:")
    proizvodProdajaMaxSale.show(20)

    // VARIJANTA 2:

    //val proizvodProdaja2 = wallmartDS.groupBy("Product_ID", "Age", "City_Category")
    //                                .agg(count("*") as "Prodaja")

    val proizvodProdajaMax2 = proizvodProdaja.groupBy("Product_ID")
                                    .agg(max("Prodaja") as "Max Prodaja")
                                    .join(proizvodProdaja, "Product_ID")
                                    .where($"Prodaja"===$"Max Prodaja")
                                    .drop("Prodaja")

    proizvodProdajaMax2.select("Product_ID", "Age", "City_Category", "Max Prodaja")
                        .orderBy($"Max Prodaja".desc)
                        .show()

    /* ZADATAK 2 - REZULTAT:

    +----------+-----+-------------+-----------+
Product_ID|  Age|City_Category|Max Prodaja|
    +----------+-----+-------------+-----------+
 P00265242|26-35|            C|        290|
 P00025442|26-35|            C|        247|
 P00112142|26-35|            C|        246|
 P00110742|26-35|            B|        243|
 P00184942|26-35|            B|        230|
 P00057642|26-35|            B|        229|
 P00058042|26-35|            B|        225|
    ...
    */

    // ZADATAK 3. - Навести које производе и које категорије производа најчешће купују жене и мушкарци неког занимања. 

    val occupationProizvodDS = wallmartDS.groupBy("Occupation", "Gender", "Product_ID")
                                    .agg(count("*") as "Amount")
                                    .orderBy($"Occupation")

    val occupKupovina = occupationProizvodDS.withColumn("Max Kupovina", max("Amount")
                                            .over(Window.partitionBy("Occupation", "Gender")))
                        
    val topOccupationKupovina = occupKupovina.@@filter($"Amount" === $"Max Kupovina")
                                            .drop("Amount")

    println("ZADATAK 3 - REZULTAT:")
    topOccupationKupovina.show()

    // Ako hocemo da piše, Žene/Muškarci:
    val topZeneMuskarci = topOccupationKupovina.withColumn("Gender", when($"Gender" === "F", "Zene").otherwise("Muskarci"))
        
    topZeneMuskarci.show()

    // -------

    val occupationProductCategory = wallmartDS.groupBy("Occupation", "Gender", "Product_Category")
                                    .agg(count("*") as "Category_Amount")
                                    .orderBy($"Occupation")

    val occupation_window = Window.partitionBy("Occupation", "Gender")

    val occupCategoryKupovina = occupationProductCategory.withColumn("Max Category Kupovina", max("Category_Amount").over(occupation_window))

    val topOccupationCategoryKupovina = occupCategoryKupovina.filter($"Category_Amount" === $"Max Category Kupovina")
                                                            .drop("Category_Amount")

    val zad3fullResult = topOccupationCategoryKupovina.join(topOccupationKupovina, Seq("Occupation", "Gender"))

    println("ZADATAK 3 - FULL REZULTAT:")
    zad3fullResult.show()
    /* ZADATAK 3 - REZULTAT:
    +----------+------+----------+------------+
Occupation|Gender|Product_ID|Max Kupovina|
    +----------+------+----------+------------+
         0|     F| P00265242|          62|
         0|     M| P00265242|         144|
         1|     F| P00265242|          59|
         1|     F| P00220442|          59|
         1|     M| P00265242|         100|
         2|     F| P00059442|          28|
    ...
    +----------+------+----------------+---------------------+
Occupation|Gender|Product_Category|Max Category Kupovina|
    +----------+------+----------------+---------------------+
         0|     F|               5|                 5657|
         0|     M|               1|                14532|
         1|     F|               5|                 5536|
         1|     M|               5|                 7633|
         2|     F|               5|                 2646|
         2|     M|               5|                 4967|
         3|     F|               5|                 2380|
    ...
    +----------+------+----------------+---------------------+----------+------------+
Occupation|Gender|Product_Category|Max Category Kupovina|Product_ID|Max Kupovina|
    +----------+------+----------------+---------------------+----------+------------+
         0|     F|               5|                 5657| P00265242|          62|
         0|     M|               1|                14532| P00265242|         144|
         1|     F|               5|                 5536| P00265242|          59|
         1|     F|               5|                 5536| P00220442|          59|
         1|     M|               5|                 7633| P00265242|         100|
         2|     F|               5|                 2646| P00059442|          28|
         2|     M|               5|                 4967| P00265242|          63|
         3|     F|               5|                 2380| P00117942|          28|
         3|     M|               5|                 2906| P00059442|          28|
         3|     M|               5|                 2906| P00025442|          28|
    ...

    */
    // ZADATAK 4.
    // Da li u nekoj zivotnoj dobi, samci muskarci vise trose nego zene u nekoj kategoriji grada? 
	// Dati primer takvih kategorija gradova i zivotnog doba. Ako ne postoji takva kombinacija zivotnog doba i kategorije grada, 
	// onda navesti zivotno doba gde je ta razlika najmanja u korist zena.

    val zivotnaDobSamci = wallmartDS.filter($"Marital_Status" ===0)
                            .groupBy("Age","City_Category")
                            .agg(avg(when($"Gender"=== "M", $"Purchase")) as "Muskarci Potrosnja", 
                                avg(when($"Gender"==="F", $"Purchase")) as "Zene Potrosnja")
                            .withColumn("Razlika Potrosnje", $"Muskarci Potrosnja"-$"Zene Potrosnja")

    val zad4res = zivotnaDobSamci.orderBy($"Age", $"City_Category")
    
    println("ZADATAK 4 - REZULTAT:")
    zad4res.show()

    // Varijanta 2

    val muskarciTrosenje = wallmartDS.filter($"Marital_Status" ===0 and $"Gender"==="M")
                            .groupBy("Age","City_Category").agg(avg("Purchase") as "Muskarci Potrosnja")
    
    val zeneTrosenje = wallmartDS.filter($"Marital_Status" ===0 and $"Gender"==="F")
                            .groupBy("Age","City_Category")
                            .agg(avg("Purchase") as "Zene Potrosnja")

    val samciDobTrosenje = muskarciTrosenje.join(zeneTrosenje, Seq("Age", "City_Category"))
                            .withColumn("Razlika Potrosnje", $"Muskarci Potrosnja"-$"Zene Potrosnja")

    samciDobTrosenje.orderBy($"Age", $"City_Category")
                            .show()

    /* REZULTAT:

    +-----+-------------+------------------+-----------------+-------------------+
  Age|City_Category|Muskarci Potrosnja|   Zene Potrosnja|  Razlika Potrosnje|
    +-----+-------------+------------------+-----------------+-------------------+
 0-17|            A| 9655.655423883318|7826.252246026262|  1829.403177857056|
 0-17|            B|  8946.03023255814|8846.238338658148|  99.79189389999192|
 0-17|            C| 9365.363024544735|8313.388701110574| 1051.9743234341604|
18-25|            A| 9044.066666666668|8558.911988180667|  485.1546784860002|
18-25|            B| 9401.546168254095|8065.421820303384| 1336.1243479507111|
18-25|            C| 9873.842563334996|9105.567637155873|  768.2749261791232|
26-35|            A| 9080.252034084662|8590.171803081848|  490.0802310028139|
26-35|            B| 9429.548563838618|8463.337348538846|  966.2112152997724|
26-35|            C|10005.376382866636|8909.458704988438| 1095.9176778781984|
36-45|            A| 9397.794833433074|8778.596976793699|  619.1978566393755|
    ...
    */


}
```


#### Short summary: 

empty definition using pc, found symbol in pc: 