package com.akka.spark.common

/**
 * 用来保存已注册的 WorkerActor的信息的类
 *
 * @param workerId : WorkerActor的 Id(UUID
 * @param cpuCores : WorkerActor的 CPU核数
 * @param memory : WorkerActor 的内存大小
 * @param lastHeartBeatTime : 最后一次心跳时间
 */
case class WorkerInfo(workerId: String, cpuCores: Int, memory: Int, lastHeartBeatTime: Long)