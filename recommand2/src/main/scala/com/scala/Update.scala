package com.scala
import java.text.SimpleDateFormat
import java.util.Date
import org.apache.spark.sql.SparkSession

object Update {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Update").master("local[4]").getOrCreate()

    val getfilename = new ScalaMysql().ReadFileName()
    val filename = Constants.FILENAME
    val hdfspath = Constants.HDFSPATH

    val mysqlDf = new ScalaMysql().ReadMysqlData()
    val df = spark.read.parquet(hdfspath + getfilename)

    val finalDf = mysqlDf.union(df)
      .groupBy("user", "product")
      .agg(Map("rating" -> "sum"))
      .withColumnRenamed("sum(rating)", "rating");

    val time = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss") format (new Date())
    val name = filename.split("\\.")(0) + "_" + time + "." + filename.split("\\.")(1)
    finalDf.coalesce(1).write.parquet(hdfspath + name)
    new ScalaMysql().updatefilename(name)

  }
}
