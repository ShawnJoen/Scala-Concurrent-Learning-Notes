package com.actor.ex0

import scala.actors.Actor

object Test1 {
  case class Message(id: Int, message: String)
  // 接收消息的 Actor
  object MsgActor extends Actor {
    override def act(): Unit = {
      loop { // 循环接收
        react { // 复用线程
          case Message(id, message) => println(s"This is MsgActor, received: ${id}, ${message}")
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    // 开启接收消息的 Actor
    MsgActor.start()
    // 从 MainActor发送异步消息, 无返回值
    MsgActor ! Message(35, "Hello MsgActor!")
  }
}
// This is MsgActor, received: 35, Hello MsgActor!
