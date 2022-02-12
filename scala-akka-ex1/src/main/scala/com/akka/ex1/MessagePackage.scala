package com.akka.ex1

/**
 * 提交任务的消息格式
 *
 * @param msg 发送信息
 */
case class SubmitTaskMessage(msg: String)

/**
 * 提交任务成功后的回执信息的格式
 *
 * @param msg 回执信息
 */
case class SuccessSubmitTaskMessage(msg: String)