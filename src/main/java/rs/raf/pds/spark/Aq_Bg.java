package rs.raf.pds.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Arrays;

import static org.apache.spark.sql.functions.*;

public class Aq_Bg {

    public static void main(String[] args) {

        // === Kreiranje Spark sesije ===
        SparkSession spark = SparkSession.builder()
                .appName("Aq_Bg")
                .master("local[*]")
                .getOrCreate();

        spark.sparkContext().setLogLevel("OFF");

        // === Učitavanje dataset-a ===
        Dataset<Row> aqDF = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("dataset/aq_belgrade.csv");

        // === Izdvajanje temperature ===
        Dataset<Row> tempDF = aqDF
                .filter(col("parameter").equalTo("temperature"))
                .select(col("datetimeUtc").alias("datetime"), col("value").alias("temperature"));

        // === Filtriranje PM čestica (pm1, pm10, pm25) ===
        Dataset<Row> pmDF = aqDF
                .filter(col("parameter").isin("pm1", "pm10", "pm25"))
                .select(col("datetimeUtc").alias("datetime"), col("parameter"), col("value"));

        // === Spajanje temperature i PM podataka po datumu/vremenu ===
        Dataset<Row> joinedDF = pmDF.join(tempDF, "datetime");

        // === Kategorizacija temperatura po zonama ===
        Dataset<Row> categorizedDF = joinedDF.withColumn(
                "temp_zone",
                when(col("temperature").leq(10), "do 10°C")
                        .when(col("temperature").gt(10).and(col("temperature").leq(20)), "10-20°C")
                        .when(col("temperature").gt(20).and(col("temperature").leq(30)), "20-30°C")
                        .otherwise("preko 30°C")
        );

        // === Pivot tabela: prosečne vrednosti PM po temperaturnim zonama ===
        Dataset<Row> pivotTemp = categorizedDF
                .groupBy("temp_zone")
                .pivot("parameter", Arrays.asList("pm1", "pm10", "pm25"))
                .agg(avg("value"))
                .orderBy("temp_zone");

        System.out.println("\nTemperaturne zone i prosečne vrednosti PM čestica:");
        pivotTemp.show(false);

        // === Prosečne vrednosti PM po mesecima ===
        Dataset<Row> pmWithMonth = pmDF.withColumn("month", month(to_timestamp(col("datetime"))));

        Dataset<Row> pivoted = pmWithMonth.groupBy("month")
                .pivot("parameter", Arrays.asList("pm1", "pm10", "pm25"))
                .agg(avg("value"))
                .orderBy("month");

        System.out.println("\nMeseci - PM koncentracije:");
        pivoted.show(12, false);

        // === Prosečna zagadjenost po satu u toku dana ===
        Dataset<Row> pmWithHour = pmDF.withColumn("hour", hour(to_timestamp(col("datetime"))));

        Dataset<Row> hourlyAvg = pmWithHour
                .groupBy("hour")
                .pivot("parameter", Arrays.asList("pm1", "pm10", "pm25"))
                .agg(avg("value"))
                .orderBy("hour");

        System.out.println("\nProsečna zagadjenost po satu:");
        hourlyAvg.show(24, false);

        // === Najzagađeniji dan u čitavom periodu (po zbiru PM1 + PM10 + PM25) ===
        Dataset<Row> pmByDay = pmDF
                .withColumn("date", to_date(col("datetime")))
                .groupBy("date")
                .pivot("parameter", Arrays.asList("pm1", "pm10", "pm25"))
                .agg(avg("value"));

        Dataset<Row> pmWithTotal = pmByDay.withColumn("total_pm",
                coalesce(col("pm1"), lit(0))
                        .plus(coalesce(col("pm10"), lit(0)))
                        .plus(coalesce(col("pm25"), lit(0)))
        );

        Dataset<Row> mostPollutedDay = pmWithTotal.orderBy(col("total_pm").desc()).limit(1);

        System.out.println("\nNajzagađeniji dan po ukupnom zbiru PM čestica:");
        mostPollutedDay.show(false);

        // === Najzagađeniji dan za svaki mesec ===
        Dataset<Row> dailyAvg = pmDF
                .withColumn("date", to_date(col("datetime")))
                .withColumn("month", month(to_timestamp(col("datetime"))))
                .groupBy("month", "date")
                .pivot("parameter", Arrays.asList("pm1", "pm10", "pm25"))
                .agg(avg("value"));

        Dataset<Row> dailyWithTotal = dailyAvg.withColumn("total_pm",
                coalesce(col("pm1"), lit(0))
                        .plus(coalesce(col("pm10"), lit(0)))
                        .plus(coalesce(col("pm25"), lit(0)))
        );

        // Kreiranje privremene SQL tabele
        dailyWithTotal.createOrReplaceTempView("daily_data");

        Dataset<Row> mostPollutedEachMonth = spark.sql("""
            SELECT month, date, pm1, pm10, pm25, total_pm
            FROM (
                SELECT *,
                       ROW_NUMBER() OVER (PARTITION BY month ORDER BY total_pm DESC) as rn
                FROM daily_data
            ) tmp
            WHERE rn = 1
            ORDER BY month
        """);

        System.out.println("\nNajzagađeniji dan u svakom mesecu:");
        mostPollutedEachMonth.show(12, false);

        // === Kvalitet vazduha po danima na osnovu PM2.5 vrednosti ===
        Dataset<Row> pm25 = pmDF
                .filter(col("parameter").equalTo("pm25"))
                .withColumn("date", to_date(col("datetime")))
                .withColumn("month", month(to_timestamp(col("datetime"))));

        Dataset<Row> dailyAvgPM25 = pm25
                .groupBy("date", "month")
                .agg(avg("value").alias("pm25_avg"));

        Dataset<Row> labeled = dailyAvgPM25.withColumn("kvalitet",
                when(col("pm25_avg").leq(12), "odličan")
                        .when(col("pm25_avg").leq(35), "umeren")
                        .when(col("pm25_avg").leq(55), "nezdrav za osetljive")
                        .when(col("pm25_avg").leq(150), "nezdrav")
                        .otherwise("vrlo nezdrav/opasan")
        );

        Dataset<Row> countByCategory = labeled.groupBy("kvalitet").count()
                .orderBy("count");

        System.out.println("\nBroj dana po kategorijama kvaliteta vazduha (PM2.5, ceo period):");
        countByCategory.show(false);

        // === Zatvaranje Spark sesije ===
        spark.stop();
    }
}
