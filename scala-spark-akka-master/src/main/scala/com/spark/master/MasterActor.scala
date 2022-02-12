package com.akka.spark.master

import java.util.Date
import akka.actor.Actor
import com.akka.spark.common.{RegisterSuccessMessage, WorkerHeartBeatMessage, WorkerInfo, WorkerRegisterMessage}

object MasterActor extends Actor {
  // 1. 定义一个可变的 Map集合, 用来保存已注册的 WorkerActor信息
  private val regWorkerMap = scala.collection.mutable.Map[String, WorkerInfo]()

  // MasterActor定期检查 WorkerActor心跳, 将超时的 Worker移除
  override def preStart(): Unit = {
    // 1. 导入时间隐式参数& 转换
    import context.dispatcher
    import scala.concurrent.duration._

    // 2. 启动定时任务(MasterActor自检移除超时的 WorkerActor)
    context.system.scheduler.schedule(0 seconds, ConfigUtils.`master.check.heartbeat.interval` seconds) {
      // 3. 过滤超时的 WorkerActor(返回被过滤的 workerId集合
      val timeOutWorkerMap = regWorkerMap.filter {
        keyVal => // keyVal的数据格式: workerId -> WorkerInfo(workerId, cpuCores, memory, lastHeartBeatTime)
          // 3.1 获取当前 WorkerActor对象的最后一次心跳时间
          val lastHeartBeatTime = keyVal._2.lastHeartBeatTime
          // 3.2 如果超时, 则 true, 否则 false (当前时间 - 最后一次心跳时间) > 最大超时时间 * 1000
          if ((new Date().getTime - lastHeartBeatTime) > (ConfigUtils.`master.check.heartbeat.timeout` * 1000)) true else false
      }

      // 4. 将要移除的已超时 workerId集合
      if (!timeOutWorkerMap.isEmpty) {
        regWorkerMap --= timeOutWorkerMap.map(_._1) // ArrayBuffer(5b9feb50-5c33-496b-a325-dd168360281b)
      }

      // 5. 有效的 WorkerActor, 按内存大小进行降序排列
      val workerList = regWorkerMap.map(_._2).toList.sortBy(_.memory).reverse
      println(s"Active WorkerActors: ${workerList}")
    }
  }

  override def receive: Receive = {
    // 接收 WorkerActor的注册信息
    case WorkerRegisterMessage(workerId, cpuCores, memory) => {
      // 打印接收到的注册信息
      println(s"MasterActor, received info: ${workerId}, ${cpuCores}, ${memory}")

      // 将注册信息保存到哈希表中& 并记录最后一次心跳时间
      regWorkerMap += workerId -> WorkerInfo(workerId, cpuCores, memory, new Date().getTime)

      // 给注册的 WorkerActor回执信息
      sender ! RegisterSuccessMessage
    }

    // 接收 WorkerActor发来的心跳消息
    case WorkerHeartBeatMessage(workerId, cpuCores, memory) => {
      println(s"MasterActor, received heartbeat: ${workerId}")
      // 更新指定的 WorkerActor对象的最后一次心跳时间
      regWorkerMap += workerId -> WorkerInfo(workerId, cpuCores, memory, new Date().getTime)
    }
  }
}