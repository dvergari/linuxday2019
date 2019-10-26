//ML import
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.ml.tuning.TrainValidationSplitModel

import org.apache.spark.sql.types._
import io.delta._


val model = TrainValidationSplitModel.load("gs://linuxday/ml/models/boston")


val kafkaDF = spark.readStream.format("kafka").option("kafka.ssl.endpoint.identification.algorithm","https").option("kafka.sasl.mechanism","PLAIN").option("kafka.request.timeout.ms","20000").option("kafka.bootstrap.servers","pkc-4r297.europe-west1.gcp.confluent.cloud:9092").option("kafka.retry.backoff.ms","500").option("kafka.sasl.jaas.config","org.apache.kafka.common.security.plain.PlainLoginModule required username='DA7BL7YCIGVXOXKH' password='byX8Scn9mqzQQKA2Shcte6hsglzIK6SzAUOE2TwWH93JT7ensM0odRXDhiWLbpyz';").option("kafka.security.protocol","SASL_SSL").option("subscribe","house-features").load()

val dataDF = kafkaDF.selectExpr("CAST(value AS STRING)")

val schema = StructType(Array(StructField("crim",DoubleType,true),StructField("zn",DoubleType,true),StructField("indus",DoubleType,true),StructField("chas",DoubleType,true),StructField("nox",DoubleType,true),StructField("rm",DoubleType,true),StructField("age",DoubleType,true),StructField("dis",DoubleType,true),StructField("rad",DoubleType,true),StructField("tax",DoubleType,true),StructField("ptratio",DoubleType,true),StructField("b",DoubleType,true),StructField("lstat",DoubleType,true)))




val features = dataDF.withColumn("tmp", split($"value", "\\,")).selectExpr("cast(tmp[0] as Double) crim", "cast(tmp[1] as Double) zn", "cast(tmp[2] as Double) indus", "cast(tmp[3] as Double) chas", "cast(tmp[4] as Double) nox", "cast(tmp[5] as Double) rm", "cast(tmp[6] as Double) age", "cast(tmp[7] as Double) dis", "cast(tmp[8] as Double) rad", "cast(tmp[9] as Double) tax", "cast(tmp[10] as Double) ptratio", "cast(tmp[11] as Double) b", "cast(tmp[12] as Double) lstat").drop("tmp")





val results = model.transform(features)

results.select("features", "prediction").writeStream.format("console").start.awaitTermination
results.select("features", "prediction").writeStream.format("delta").outputMode("append").option("checkpointLocation","gs://linuxday/delta/_checkpoints/prediction").start("gs://linuxday/delta/output").awaitTermination

