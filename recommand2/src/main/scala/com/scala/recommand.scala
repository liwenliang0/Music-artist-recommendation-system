package com.scala

/**
  * Created by li on 2018/1/27.
  */
import java.text.SimpleDateFormat
import java.util.Date
import org.apache.spark.mllib.recommendation._
//import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark
//import org.apache.spark.ml.recommendation.ALS.Rating
import scala.collection.Map
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.recommendation._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
object recommand {
  def loadmodel(): MatrixFactorizationModel ={
    val spark = SparkSession.builder().appName("recommand").master("local[*]").getOrCreate()
    spark.conf.set("spark.executor.memory", "3g")
    spark.conf.set("spark.driver.maxResultSize", "3g")
   val modelname=new ScalaMysql().ReadmodelName()
    val newmodel=MatrixFactorizationModel.load(spark.sparkContext,
      "hdfs://172.17.11.162:9000/outputmodel/"+modelname )
    newmodel
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("recommand").master("local[4]").getOrCreate()
    val model:MatrixFactorizationModel=loadmodel()
    val userID = 2093760
    val recommendations = model.recommendProducts(userID, 5)
    val recommendedProductIDs = recommendations.map(_.product).toSet

    val artist_id_name = spark.sparkContext.textFile(Constants.HDFSPATH+"data/newArtist_id_name.txt")
     .map(line=> {
       val id = line.split(",")(0).substring(1).toInt
       val len=line.split(",")(1).length
       val name = line.split(",")(1).replace(")","")
       (id, name)
      }
     )
    val result=artist_id_name.filter { case (id, name) => recommendedProductIDs.contains(id) }.values


    import spark.implicits._
    result.toDF()
    val time = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss") format (new Date())
    result.saveAsTextFile("hdfs://172.17.11.162:9000/music/recommandresult/recommand_"+time+".txt")
  }
}
