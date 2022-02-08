package com.actor.ex0.wordcount

import scala.actors.Actor
import scala.io.Source

class WordCountActor extends Actor {
  override def act(): Unit = {
    loop {
      react {
        case WordCountTask(fileName) =>
          // 4) WordCountActor接收消息, 并统计单个文件的单词计数
          // 4-1) 按行读取, 文件内容
          val lineList = Source.fromFile(fileName).getLines().toList
          println(s"WordCountTask${fileName}: " + lineList) // List(hadoop sqoop hadoop, hadoop hadoop flume, hadoop hadoop hadoop, spark)

          // 4-2) 将上述获取到的数据, 转换成一个个单词
          val strList = lineList.flatMap(_.split(" "))
          println(s"WordCountTask${fileName}: " + strList) // List(hadoop, sqoop, hadoop, hadoop, hadoop, flume, hadoop, hadoop, hadoop, spark)

          // 4-3) 给每一个单词, 后边加上次数, 默认为1
          val wordAndCount = strList.map(_ -> 1)
          println(s"WordCountTask${fileName}: " + wordAndCount)
          // List((hadoop,1), (sqoop,1), (hadoop,1), (hadoop,1), (hadoop,1), (flume,1), (hadoop,1), (hadoop,1), (hadoop,1), (spark,1))

          // 4-4) 进行分组
          val groupMap = wordAndCount.groupBy(_._1)
          println(s"WordCountTask${fileName}: " + groupMap)
          // Map(spark -> List((spark,1)), hadoop -> List((hadoop,1), (hadoop,1), (hadoop,1), (hadoop,1), (hadoop,1), (hadoop,1), (hadoop,1)),
          //      sqoop -> List((sqoop,1)), flume -> List((flume,1)))

          // 4-5) 统计每个单词的总数量
          val wordCountMap = groupMap.map(keyVal => keyVal._1 -> keyVal._2.map(_._2).sum)
          println(s"WordCountTask${fileName}: " + wordCountMap) // Map(spark -> 1, hadoop -> 7, sqoop -> 1, flume -> 1)

          // 5) 将单词计数结果, 发送给 MainActor
          sender ! WordCountResult(wordCountMap)
      }
    }
  }
}
