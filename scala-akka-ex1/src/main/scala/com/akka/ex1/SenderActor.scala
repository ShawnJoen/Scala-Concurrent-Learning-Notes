package com.akka.ex1

import akka.actor.Actor

object SenderActor extends Actor {
  override def receive: Receive = {
    // 接收 Entrance发的消息: start
    case "start" => {
      println("SenderActor, received: start!")
      // 获取 ReceiverActor的路径
      val receiverActor = context.actorSelection("akka://actorSystem/user/receiverActor")
      // 给 ReceiverActor发送消息
      receiverActor ! SubmitTaskMessage("Hello ReceiverActor!, This is SenderActor.")
    }
    // 接收 ReceiverActor返回的回执信息
    case SuccessSubmitTaskMessage(msg) => println(s"SenderActor, received: ${msg}")
  }
}
