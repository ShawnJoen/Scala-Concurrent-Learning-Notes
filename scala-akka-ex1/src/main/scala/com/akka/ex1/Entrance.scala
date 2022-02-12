package com.akka.ex1

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Entrance {
  def main(args: Array[String]): Unit = {
    /**
     * 创建ActorSystem, 用来负责创建和监督 Actor
     *
     * @param name : scala.Predef.String 给 ActorSystem设置名字
     * @param config : com.typesafe.config.Config 配置环境
     */
    val actorSystem = ActorSystem("actorSystem", ConfigFactory.load())

    /**
     * 通过 ActorSystem来加载自定义 Actor对象
     *
     * @param props : akka.actor.Props 指定要管理的 Actor伴生对象
     * @param name : scala.Predef.String 给指定 Actor对象设置名称
     */
    val senderActor = actorSystem.actorOf(Props(SenderActor), "senderActor")
    // 必须给每个 Actor设置名称, 否则无法从 SenderActor内部通过 context.actorSelection方式, 获得 ReceiverActor的对象
    // 而会提示 Actor[akka://actorSystem/user/receiverActor] was not delivered
    actorSystem.actorOf(Props(ReceiverActor), "receiverActor")

    // 给 SenderActor发送 "start"字符串
    senderActor ! "start"
  }
}
