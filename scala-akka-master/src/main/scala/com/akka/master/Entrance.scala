package com.akka.master

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Entrance {
  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem("actorSystem", ConfigFactory.load())
    val masterActor = actorSystem.actorOf(Props(MasterActor), "masterActor")
    // 给 MasterActor发送消息
    masterActor ! "setup"
  }
}
