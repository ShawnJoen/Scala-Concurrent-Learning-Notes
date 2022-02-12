package com.akka.spark.master

import com.typesafe.config.{Config, ConfigFactory}

// 用来读取配置文件信息的类
object ConfigUtils {
  // 1. 获取配置文件对象
  private val config: Config = ConfigFactory.load()
  // 2. 获取检查 WorkerActor心跳的时间间隔
  val `master.check.heartbeat.interval` = config.getInt("master.check.heartbeat.interval")
  // 3. 获取 WorkerActor心跳超时时间
  val `master.check.heartbeat.timeout` = config.getInt("master.check.heartbeat.timeout")
}