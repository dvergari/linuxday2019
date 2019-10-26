name := "BostonHousingPrice"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.4.4" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.4.4" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.4.4" % "provided",
  "io.delta" %% "delta-core" % "0.4.0"
)


/*
assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

fork in Test := true
test in assembly := {}

assemblyOutputPath in assembly := file("target/scala-2.11/" + name.value + "-" + version.value + ".jar")
*/