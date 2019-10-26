import org.apache.spark.ml.tuning.TrainValidationSplitModel
import io.delta.tables.DeltaTable
import org.apache.spark.mllib.evaluation.RegressionMetrics

val model = TrainValidationSplitModel.load("gs://linuxday/ml/models/boston")

val test = DeltaTable.forPath("gs://linuxday/delta/test_data").toDF

val result = model.transform(test)

result.printSchema

val prediction = result.select(result("prediction"), result("label")).as[(Double, Double)].rdd

val metrics = new RegressionMetrics(prediction)


println(metrics.r2)
