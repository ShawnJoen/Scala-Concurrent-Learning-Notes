package com.akka.spark.common

/**
 * WorkerActor提交注册信息的类
 *
 * @param workerId : WorkerActor的 Id(UUID
 * @param cpuCores : WorkerActor的 CPU核数
 * @param memory : WorkerActor 的内存大小
 */
case class WorkerRegisterMessage(workerId: String, cpuCores: Int, memory: Int)

/** 注册成功后回执的单例对象*/
case object RegisterSuccessMessage

/**
 * WorkerActor定时触发心跳到 MasterActor的信息类
 *
 * @param workerId : WorkerActor的 Id(UUID
 * @param cpuCores : WorkerActor的 CPU核数
 * @param memory : WorkerActor 的内存大小
 */
case class WorkerHeartBeatMessage(workerId: String, cpuCores: Int, memory: Int)