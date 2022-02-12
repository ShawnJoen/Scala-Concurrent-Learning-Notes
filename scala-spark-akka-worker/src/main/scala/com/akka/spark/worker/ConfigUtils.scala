package com.akka.spark.worker

import com.typesafe.config.{Config, ConfigFactory}

// 用来读取配置文件信息的类
object ConfigUtils {
  // 1. 获取配置文件对象
  private val config: Config = ConfigFactory.load()
  // 2. 获取 WorkerActor心跳的间隔时间
  val `worker.heartbeat.interval`: Int = config.getInt("worker.heartbeat.interval")
}