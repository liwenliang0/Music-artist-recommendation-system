package com.scala
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import java.sql.{Connection, DriverManager}
import org.apache.spark.sql.functions._
import scala.collection._

class ScalaMysql {
  val ip=Constants.JDBC_IP
  val schema=Constants.JDBC_SCHEMA
  val user=Constants.JDBC_USER
  val pass=Constants.JDBC_PASSWORD
  def ReadMysqlData():DataFrame= {
    val today= "2018-01-16"
    // val today= new SimpleDateFormat("yyyy-MM-dd")format(new Date())
    val spark = SparkSession.builder().appName("ReadMysqlData").master("local[4]").getOrCreate()
    val df =spark.read.format("jdbc")
      .options(Map("url" -> new String("jdbc:mysql://"+ip+":3306/"+schema),
        "dbtable" -> "tb_sys_user_music",
        "user" -> user, "password" -> pass)).load()
    df.withColumn("rating",lit(1.0))
      .where("UPDATE_TIME >'"+today+"'")
      .select("USER_ID","MUSIC_ID","rating")
      .withColumnRenamed("USER_ID","user")
      .withColumnRenamed("MUSIC_ID","product")
  }

  def ReadFileName():String={
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://"+ip+"/"+schema
    val username = user
    val password = pass
    var connection:Connection = null
    var name = ""
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)
      val statement = connection.createStatement()
      val resultSet = statement.executeQuery("select * from lastfile")
      while ( resultSet.next() )
         name = resultSet.getString("name")
    connection.close()
    name
  }

  def ReadmodelName():String={
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://"+ip+"/"+schema
    val username = user
    val password = pass
    var connection:Connection = null
    var name = ""
    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery("select * from latestmodel")
    while ( resultSet.next() )
      name = resultSet.getString("name")
    connection.close()
    name
  }

  def updatefilename(time:String):Unit={
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://"+ip+"/"+schema
    val username = user
    val password = pass
    var connection:Connection = null
    var name = ""
    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)
    val ps = connection.prepareStatement("UPDATE lastfile SET name = ? WHERE id = ?")
    ps.setString(1, time)
    ps.setInt(2, 1)
    ps.executeUpdate()
    ps.close()
    connection.close()
  }

  def updatemodelname(modelname:String): Unit ={
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://"+ip+"/"+schema
    val username = user
    val password = pass
    var connection:Connection = null
    var name = ""
    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)
    val ps = connection.prepareStatement("UPDATE latestmodel SET name = ? WHERE id = 1")
    ps.setString(1, modelname)
    ps.executeUpdate()
    ps.close()
    connection.close()
  }
}
