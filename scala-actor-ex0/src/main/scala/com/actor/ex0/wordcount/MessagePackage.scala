package com.actor.ex0.wordcount

/**
 * 表示: MainActor给每个 WordCountActor发送消息的格式
 *
 * @param fileName 具体要统计的文件全路径
 */
case class WordCountTask(fileName: String)

/**
 * 每个 WordCountActor返回结果的格式
 *
 * @param wordCountMap 返回结果 如: Map("hadoop" -> 6, "sqoop" -> 1)
 */
case class WordCountResult(wordCountMap: Map[String, Int])