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
object model {

  def areaUnderCurve(
                      positiveData: RDD[Rating],
                      bAllItemIDs: Broadcast[Array[Int]],
                      predictFunction: (RDD[(Int,Int)] => RDD[Rating])) = {

    val positiveUserProducts = positiveData.map(r => (r.user, r.product))
    val positivePredictions = predictFunction(positiveUserProducts).groupBy(_.user)
    val negativeUserProducts = positiveUserProducts.groupByKey().mapPartitions {
      userIDAndPosItemIDs => {
        val random = new Random()
        val allItemIDs = bAllItemIDs.value
        userIDAndPosItemIDs.map { case (userID, posItemIDs) =>
          val posItemIDSet = posItemIDs.toSet
          val negative = new ArrayBuffer[Int]()
          var i = 0
          while (i < allItemIDs.size && negative.size < posItemIDSet.size) {
            val itemID = allItemIDs(random.nextInt(allItemIDs.size))
            if (!posItemIDSet.contains(itemID)) {
              negative += itemID
            }
            i += 1
          }
          negative.map(itemID => (userID, itemID))
        }
      }
    }.flatMap(t => t)
    val negativePredictions = predictFunction(negativeUserProducts).groupBy(_.user)
    positivePredictions.join(negativePredictions).values.map {
      case (positiveRatings, negativeRatings) =>
        var correct = 0L
        var total = 0L
        for (positive <- positiveRatings;
             negative <- negativeRatings) {
          if (positive.rating > negative.rating) {
            correct += 1
          }
          total += 1
        }
        correct.toDouble / total
    }.mean()
  }

  def bestparam(
                sc: SparkContext,
                alldata:RDD[Rating]): Seq[((Int,Double,Double),Double)] = {
    val Array(trainData, cvData) = alldata.randomSplit(Array(0.9, 0.1))
    trainData.cache()
    cvData.cache()
    val allItemIDs = alldata.map(_.product).distinct().collect()
    val bAllItemIDs = sc.broadcast(allItemIDs)
    val evaluations =
      for (rank   <- Array(10,  50);
           lambda <- Array(1.0, 0.0001);
           alpha  <- Array(1.0, 40))
        yield {
          val model = ALS.trainImplicit(trainData, rank, 10, lambda, alpha)
          val auc = areaUnderCurve(cvData, bAllItemIDs, model.predict)
          ((rank, lambda, alpha), auc)
        }
    evaluations.sortBy(_._2).reverse
    val bestpara=Seq(evaluations(0))
    bestpara
  }

  def savemodel(sc:SparkContext,model: MatrixFactorizationModel): Unit ={
    val time = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss") format (new Date())
    model.save(sc,"hdfs://172.17.11.162:9000/outputmodel/mymodel"+time)
    new ScalaMysql().updatemodelname("mymodel"+time)
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("model").master("local[*]").getOrCreate()
    spark.conf.set("spark.executor.memory", "3g")
    spark.conf.set("spark.driver.maxResultSize", "3g")
    val getfilename = new ScalaMysql().ReadFileName()
    val df = spark.read.parquet(Constants.HDFSPATH+getfilename)
    val traindata=df.rdd
    val train=traindata.map(line=> Seq(line(0).toString.toInt, line(1).toString.toInt, line(2).toString.toDouble))
    val ratings = train.map { case Seq(user, item, rate) =>
      Rating(user.toInt, item.toInt, rate.toDouble)
    }
    val trainData = ratings.cache()
    val result= bestparam(spark.sparkContext,trainData)
    val rank=result(0)._1._1
    val lambda=result(0)._1._2
    val alpha=result(0)._1._3
    val model = ALS.trainImplicit(trainData, rank, 5, lambda, alpha)
    savemodel(spark.sparkContext,model)
  }
}
