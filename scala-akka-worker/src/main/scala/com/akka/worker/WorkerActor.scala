package com.akka.worker

import akka.actor.Actor

// WorkerActor的路径: akka.tcp://actorSystem@127.0.0.1:8081/user/workerActor
object WorkerActor extends Actor {
  override def receive: Receive = {
    case "setup" => {
      println("WorkerActor started!")
      // 远程获取 MasterActor
      val masterActor = context.system.actorSelection("akka.tcp://actorSystem@127.0.0.1:8080/user/masterActor")
      // 给 MasterActor发送字符串 connect
      masterActor ! "connect"
    }
    // 接收 MasterActor发的消息
    case "success" => println("MasterActor, received: success!")
  }
}
