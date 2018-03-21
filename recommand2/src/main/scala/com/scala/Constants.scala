package com.scala

object Constants {
  val FILENAME="trainData_parquet.parquet"
  val HDFSPATH="hdfs://172.17.11.162:9000/music/"
  val JDBC_IP="l72.17.11.70"
  val JDBC_SCHEMA="music"
  val JDBC_USER="root"
  val JDBC_PASSWORD="root"
}
case class Player(user:String,product:String,rating:String)