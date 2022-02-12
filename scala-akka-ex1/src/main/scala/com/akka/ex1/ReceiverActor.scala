package com.akka.ex1

import akka.actor.Actor

object ReceiverActor extends Actor {
  override def receive: Receive = {
    // 接收 SenderActor发的消息
    case SubmitTaskMessage(msg) => {
      println(s"ReceiverActor, received: ${msg}")
      // 给 SenderActor回复信息
      sender ! SuccessSubmitTaskMessage("Hi!, This is ReceiverActor.")
    }
  }
}
