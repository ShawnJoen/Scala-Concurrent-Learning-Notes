package com.akka.master

import akka.actor.Actor

object MasterActor extends Actor {
  override def receive: Receive = {
    case "setup" => println("MasterActor started!")
    // 接收 WorkerActor发的消息
    case "connect" => {
      println("MasterActor, received: connect!")
      // 给发送者(WorkerActor)返回的回执信息
      sender ! "success"
    }
  }
}
