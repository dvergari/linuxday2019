package org.outofbounds.linuxday



import org.apache.spark.sql.SparkSession
import io.delta.tables.DeltaTable
import org.apache.spark.ml.regression.DecisionTreeRegressor
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.Pipeline

object BostonHousingTraining {



  def loadDeltaTable(path: String): DeltaTable = {
    DeltaTable.forPath(path)
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
    .appName("Train Boston Housing prices")
    .getOrCreate()

    if (args.length < 2) {
      println("Usage: org.outofbounds.linuxday.BostonHousingTraining [source_delta_table] [model_path]")
      spark.close()
      System.exit(1)
    }

    val sourceTable = args(0)
    val modelPath = args(1)

    //Load the Delta Lake Table as a DataFrame
    val bostonTable = loadDeltaTable(sourceTable).toDF

    //Create a DF with the features
    val features = bostonTable.select("rm","lstat","ptratio")
    //val features = bostonTable.select("rm","age","ptratio","crim")
    //val features = bostonTable.select("rm","lstat","ptratio","crim")

    //Split the source in testing and training DF
    val Array(training, test) = bostonTable
      .withColumnRenamed("MEDV","label")
      .randomSplit(Array(0.8,0.2), seed=42)

    // Create the regressor Tree and the grid
    val regressor = new DecisionTreeRegressor()

    val grid = new ParamGridBuilder()
      .addGrid(regressor.impurity, Array("variance"))
      .addGrid(regressor.maxDepth, Array(10))
      .build()

    val featureColumnsNames = features.columns

    val assembler = new VectorAssembler()
      .setInputCols(featureColumnsNames)
      .setOutputCol("features")

    val pipeline = new Pipeline()
      .setStages(Array(assembler,regressor))

    val trainValidationSplit = new TrainValidationSplit()
      .setEstimator(pipeline)
      .setEvaluator(new RegressionEvaluator())
      .setEstimatorParamMaps(grid)
      .setTrainRatio(0.8)

    val model = trainValidationSplit.fit(training)

    model.write.overwrite.save(modelPath)

    test.write.format("delta").mode("overwrite").save("gs://linuxday/delta/test_data")
    spark.close()
    System.exit(0)
  }



}
