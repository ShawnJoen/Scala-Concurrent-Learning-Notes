package com.akka.spark.worker

import java.util.UUID
import akka.actor.{Actor, ActorSelection}
import com.akka.spark.common.{RegisterSuccessMessage, WorkerHeartBeatMessage, WorkerRegisterMessage}
import scala.util.Random

object WorkerActor extends Actor {
  // 表示 MasterActor的引用
  private var masterActor: ActorSelection = _
  // WorkerActor的注册信息
  private var workerId: String = _
  private var cpuCores: Int = _ // CPU核数
  private var memory: Int = _ // 内存大小
  private val cpuCoreList = List(1, 2, 3, 4, 6, 8) // CPU核心数的随机取值范围
  private val memoryList = List(512, 1024, 2048, 4096) // 内存大小的随机取值范围

  override def preStart(): Unit = {
    // 获取 MasterActor的引用
    masterActor = context.system.actorSelection("akka.tcp://actorSystem@127.0.0.1:8080/user/masterActor")

    // 随机设置编号
    workerId = UUID.randomUUID().toString

    // 随机选择 WorkerActor的 CPU核数和内存大小
    val r = new Random()
    cpuCores = cpuCoreList(r.nextInt(cpuCoreList.length))
    memory = memoryList(r.nextInt(memoryList.length))

    // 封装 WorkerActor的注册信息
    val registerMessage = WorkerRegisterMessage(workerId, cpuCores, memory)
    // 发给 MasterActor
    masterActor ! registerMessage
  }

  override def receive: Receive = {
    // 注册成功的回执信息
    case RegisterSuccessMessage => {
      println("Connection is successful!")

      // 1. 导入时间隐式参数& 转换
      import context.dispatcher
      import scala.concurrent.duration._

      // 定时给 MasterActor发心跳消息
      context.system.scheduler.schedule(0 seconds, ConfigUtils.`worker.heartbeat.interval` seconds) {
        masterActor ! WorkerHeartBeatMessage(workerId, cpuCores, memory)
      }
    }
  }
}
