package com.actor.ex0

import scala.actors.Actor

object Test2 {
  case class Message(id: Int, message: String)
  case class ReplyMessage(message: String, name: String)

  object MsgActor extends Actor {
    override def act(): Unit = {
      loop { // 循环接收
        react { // 复用线程
          case Message(id, message) => {
            println(s"This is MsgActor, received: ${id}, ${message}")
            // sender: 表示发送者 (目前发送者是 MainActor
            sender ! ReplyMessage("I`m fine", "Man")
          }
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    MsgActor.start()
    // 发送同步消息, 等待返回值
    val result: Any = MsgActor !? Message(35, "Hello MsgActor!")
    // 将接收的返回值, 向下转型
    val reply: ReplyMessage = result.asInstanceOf[ReplyMessage]
    println(s"This is MainActor, received: ${reply.message}, ${reply.name}")
  }
}
//This is MsgActor, received: 35, Hello MsgActor!
//This is MainActor, received: I`m fine, Man
