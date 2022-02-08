package com.actor.ex0

import scala.actors.{Actor, Future}

object Test3 {
  case class Message(id: Int, message: String)
  case class ReplyMessage(message: String, name: String)

  object MsgActor extends Actor {
    override def act(): Unit = {
      loop {
        react {
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

    //    val result = MsgActor !! Message(20, "Hello, This is MainActor")
    //    println(result) // <function0>

    val future: Future[Any] = MsgActor !! Message(35, "Hello, This is MainActor")
    // 如果没有接收到回复信息, 这里就一直死循环
    while (!future.isSet) {}
    // 获取返回值
    val reply: ReplyMessage = future.apply().asInstanceOf[ReplyMessage]
    println(s"This is MainActor, received: ${reply.message}, ${reply.name}")
    //    This is MsgActor, received: 35, Hello, This is MainActor
    //    This is MainActor, received: I`m fine, Man
  }
}