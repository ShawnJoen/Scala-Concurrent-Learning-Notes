package com.actor.ex0.wordcount

import java.io.File
import scala.actors.Future

object MainActor {
  def main(args: Array[String]): Unit = {
    // 1) MainActor获取要进行单词统计的文件
    // 1-1) 获取 scala-actor-ex0/data/ 目录下的所有文件名称
    var dir = "scala-actor-ex0/data/"
    var fileNameList = new File(dir).list().toList
    println("MainActor: " + fileNameList) // List(1.txt, 2.txt)

    // 1-2) 对获取到的文件名进行拼接, 使其 scala-actor-ex0/data/ + 文件名称
    val fileDirList = fileNameList.map(dir + _)
    println("MainActor: " + fileDirList) // List(scala-actor-ex0/data/1.txt, scala-actor-ex0/data/2.txt)


    // 2) 根据文件数量, 创建对应的 WordCountActor
    val wordCountList = fileNameList.map(_ => new WordCountActor)
    println("MainActor: " + wordCountList) // List(com.actor.ex0.wordcount.WordCountActor@531be3c5, com.actor.ex0.wordcount.WordCountActor@52af6cff)

    // 2-1) 将每个 WordCountActor对象, 与目标文件全路径关联
    val actorWithFile = wordCountList.zip(fileDirList)
    println("MainActor: " + actorWithFile) // List((com.actor.ex0.wordcount.WordCountActor@531be3c5,scala-actor-ex0/data/1.txt), (com.actor.ex0.wordcount.WordCountActor@52af6cff,scala-actor-ex0/data/2.txt))


    // 3) 将文件全路径封装为消息, 启动 WordCountActor, 并 (!!)发送异步消息, 返回值是 Future[Any]
    // futureList是所有 WordCountActor的统计结果
    val futureList: List[Future[Any]] = actorWithFile.map {
      keyVal =>
        // 3.1) 获取具体要开启的 WordCountActor对象
        val actor = keyVal._1
        // 3.2) 启动 WordCountActor对象
        actor.start()
        // 3.3) 给 WordCountActor发送消息(文件路径), 并接收异步返回值
        val future: Future[Any] = actor !! WordCountTask(keyVal._2)
        future
    }


    // 6) MainActor等待接收所有 WordCountActor的异步返回值, 直到接收到消息后, 开始进行结果合并
    while (futureList.filter(!_.isSet).size != 0) {}

    // 从每个 future中, 获取数据
    val wordCountResultList = futureList.map(_.apply().asInstanceOf[WordCountResult])
    println("MainActor: " + wordCountResultList);
    // List(WordCountResult(Map(spark -> 1, hadoop -> 7, sqoop -> 1, flume -> 1)), WordCountResult(Map(sqoop -> 1, flink -> 1, hadoop -> 6, spark -> 1, hive -> 1)))

    val wordCountMapList = wordCountResultList.map(_.wordCountMap)
    println("MainActor: " + wordCountMapList);
    // List(Map(spark -> 1, hadoop -> 7, sqoop -> 1, flume -> 1), Map(sqoop -> 1, flink -> 1, hadoop -> 6, spark -> 1, hive -> 1))

    // 统计
    //  flatten: List(spark -> 1, hadoop -> 7, sqoop -> 1, flume -> 1,sqoop -> 1, flink -> 1, hadoop -> 6, spark -> 1, hive -> 1)
    //  groupBy: "spark" -> List(spark -> 1, spark -> 1) "hadoop" -> List(hadoop -> 7,  hadoop -> 6)
    //  map: "spark" -> 2, "hadoop" -> 13
    val result = wordCountMapList
      .flatten
      .groupBy(_._1)
      .map(
        keyVal => (keyVal._1 -> keyVal._2.map(_._2).sum)
      )
      .toList
      .sortWith(_._2 > _._2)
    println("MainActor: " + result) // List((hadoop,13), (sqoop,2), (spark,2), (flink,1), (hive,1), (flume,1))
  }
}