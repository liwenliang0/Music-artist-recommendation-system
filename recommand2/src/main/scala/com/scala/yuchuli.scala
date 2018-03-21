package com.scala

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.Map

object yuchuli{

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[4]").appName("app").getOrCreate()
    import spark.implicits._
    val rawUserArtistData = spark.sparkContext.textFile("c://csvfile/sparkpro/user_artist_data.txt")
    val rawArtist_id_name = spark.sparkContext.textFile("c://csvfile/sparkpro/artist_data.txt")
    val rawArtistAlias = spark.sparkContext.textFile("c://csvfile/sparkpro/artist_alias.txt")

    def buildArtist_id_name(rawArtistData: RDD[String]) =
      rawArtistData.flatMap { line =>
        val (id, name) = line.span(_ != '\t')
        if (name.isEmpty) {
          None
        } else {
          try {
            Some((id.toInt, name.trim))
          } catch {
            case e: NumberFormatException => None
          }
        }
      }
    val newArtist_id_name = buildArtist_id_name(rawArtist_id_name)



    def buildArtistAlias(rawArtistAlias: RDD[String]):Map[Int,Int]=
      rawArtistAlias.flatMap { line =>
        val tokens = line.split('\t')
        if (tokens(0).isEmpty) {
          None
        } else {
          Some((tokens(0).toInt, tokens(1).toInt))
        }
      }.collectAsMap()

    def buildRatings(
                      rawUserArtistData: RDD[String],
                      bArtistAlias: Broadcast[Map[Int,Int]]) = {
      rawUserArtistData.map { line =>
        val Array(userID, artistID, count) = line.split(' ').map(_.toInt)
        val finalArtistID = bArtistAlias.value.getOrElse(artistID, artistID)
        Rating(userID, finalArtistID, count)
      }
    }

    val bArtistAlias = spark.sparkContext.broadcast(buildArtistAlias(rawArtistAlias))
    val trainData = buildRatings(rawUserArtistData, bArtistAlias)
    val newArtist_id_name_parquet=newArtist_id_name.toDF()
    val trainData_parquet=trainData.toDF()
    newArtist_id_name.collect().take(10).foreach(println)
    println("=================================================")
    trainData_parquet.collect().take(10).foreach(println)
   newArtist_id_name_parquet.write.save("hdfs://172.17.11.162:9000/lwl/newArtist_id_name.parquet")
    trainData_parquet.write.save("hdfs://172.17.11.162:9000/lwl/trainData_parquet.parquet")



  }
}
