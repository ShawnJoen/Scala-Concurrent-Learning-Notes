package com.akka.ex2

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object MainActor {
  object ReceiverActor extends Actor {
    override def receive: Receive = {
      case x => println(x)
    }
  }

  def main(args: Array[String]): Unit = {
    // 创建ActorSystem, 用来负责创建和监督 Actor
    val actorSystem = ActorSystem("actorSystem", ConfigFactory.load())
    // 通过 ActorSystem来加载自定义 Actor对象
    val receiverActor = actorSystem.actorOf(Props(ReceiverActor), "receiverActor")

    // 导入隐式参数& 转换
    import actorSystem.dispatcher
    import scala.concurrent.duration._

    // 通过定时器, 定时给 ReceiverActor发送消息
    // 方式 1: 采用提供的 Any数据类型参数的消息
    actorSystem.scheduler.schedule(3 seconds, 2 seconds, receiverActor, "Hello ReceiverActor!, 111.")

    // 方式 2: 采用自定义函数的消息
    actorSystem.scheduler.schedule(0 seconds, 5 seconds) {
      receiverActor ! "Hello ReceiverActor!, 222."
    }
  }
}
