package com.scala
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
object ArtistChange {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[4]").appName("ArtistChange").getOrCreate()
    import org.apache.spark.sql.SQLContext
    val sqlContext = new SQLContext(spark.sparkContext)
    val hdfspath=Constants.HDFSPATH
    val filename=Constants.FILENAME

    val rdd = spark.sparkContext.textFile(hdfspath+"data/user_artist_data.txt")
      .filter(line => line.split(" ").length == 3)
      .map(word => (word.split(" ")(1), Array(word.split(" ")(0), word.split(" ")(2))))

    val rdd1 = spark.sparkContext.textFile(hdfspath+"data/artist_alias.txt")
      .filter(line => line.split("\t").length==2)
      .map(word => (word.split("\t").head,word.split("\t").last))

    var temp = rdd.leftOuterJoin(rdd1).map(word => word._2._2 match {
      case None   => (word._2._1,word._1)
      case Some(s)   => (word._2._1,s)})
      .map(word =>Player(word._1(0),word._2,word._1(1)))

    def buildArtist_id_name(rawArtistData: RDD[String]) =
      rawArtistData.flatMap { line =>
        val (id, name) = line.span(_ != '\t')
        if (name.isEmpty) {
          None
        } else {
          try {
            Some(id.toInt, name.trim)
          } catch {
            case e: NumberFormatException => None
          }
        }
      }

    val Artist_id_name=spark.sparkContext.textFile(hdfspath+"data/artist_data.txt")

    val newArtist_id_name = buildArtist_id_name(Artist_id_name)
    newArtist_id_name.saveAsTextFile(Constants.HDFSPATH+"data/newArtist_id_name.txt")

    sqlContext.createDataFrame(temp)
      .coalesce(1).write.parquet(hdfspath + filename)
    spark.sparkContext.stop()
  }

}
