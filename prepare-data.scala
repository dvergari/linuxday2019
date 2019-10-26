import io.delta.tables.DeltaTable

val df = spark.read.format("CSV").option("inferSchema","true").option("header","true").load("gs://linuxday/ml/rawdata/BostonHousing.csv")


df.write.format("delta").mode("overwrite").save("gs://linuxday/delta/BostonHousing")

val dt = DeltaTable.forPath("gs://linuxday/delta/BostonHousing")

dt.toDF.show

dt.updateExpr(
  Map("medv" -> "medv * 10000")
)


