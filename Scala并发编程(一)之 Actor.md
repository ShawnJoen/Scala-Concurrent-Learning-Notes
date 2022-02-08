# Java并发编程对比 Actor并发编程
- Java并发编程: 多线程读写共享资源时, 会将每个线程同步加锁, 并通过对象的逻辑监视器(Monitor)来控制共享资源的访问. 此时会发生资源争夺(竞态条件/Race condition), 及死锁的问题.
- Scala的 Actor并发编程: 是通过消息传递的形式(基于事件)处理多线程并发请求, 该并发编程模型无共享资源, 所以无资源争夺和死锁等问题

`*注: Actor模型, 在 Scala 2.11.x版本中加入 Akka并发编程框架后, 已经废弃! 在此学习已废弃的 Actor模型的目的为学习 Akka做准备!`
```
Java线程模型| Scala Actor模型
--|:--:
共享数据& 锁 | 无共享资源
通过每个 Object的 Monitor监视器来控制线程对共享数据的访问 | Actor之间通过 Message通讯
每个线程内部是顺序执行的 | 每个 Actor内部是顺序执行的
```
# 创建 Actor步骤
1) 可以通过类(class)或单例对象(object), 继承 Actor特质
2) 重写 act()方法
3) 调用 start()方法, 启动 Actor(自动执行, 内部 act()方法

`*注: 每个 Actor是并行执行的, 互不干扰`

`*如果在程序中创建多个相同的 Actor, 就使用 class继承 Actor特质, 或程序中相同的 Actor只启动一个, 就使用 object继承 Actor特质`

# Actor发送& 接收消息
## 发送消息
```
操作符号| 描述
--|:--:
!	| 发送异步消息, 无返回值
!?	| 发送同步消息, 等待返回值
!!	| 发送异步消息, 返回值是 Future[Any]
```
## 接收消息
```
方式| 描述
--|:--:
receive {case x: type => }	| 只接收一次消息(接收一次就会结束, 该 Actor
while(true) { receive {case x: type => } }	| 可持续接收消息, 但这个方式每次有新的消息来时, 都会重新创建新的线程来处理, 并阻塞(因此不建议使用)
loop { react { case x: type => } }	| 可持续接收消息, 可复用线程(推荐使用)
```
# 实例 1: (!) 发送异步消息, 无返回值
```
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

```

# 实例 2: (!?) 发送同步消息, 等待返回值
```
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

```

# 实例 3: (!!) 发送异步消息, 返回值是 Future[Any]
```
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

```

# WordCount案例(单词计数)
- 使用 Actor并发编程模型来实现, `多文件的单词统计`(所有单词以空格分隔)
> - 思路:
> 1) MainActor获取要进行单词统计的文件
> 2) 根据文件数量, 创建对应的 WordCountActor
> 3) 将文件全路径封装为消息, 并发送给 WordCountActor
> 4) WordCountActor接收消息, 并统计单个文件的单词计数
> 5) 将单词计数结果, 发送给 MainActor
> 6) MainActor等待接收所有 WordCountActor的异步返回值, 直到接收到消息后, 开始进行结果合并
```
1.txt
hadoop sqoop hadoop
hadoop hadoop flume
hadoop hadoop hadoop
spark

2.txt
flink hadoop hive
hadoop sqoop hadoop
hadoop hadoop hadoop
spark

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

```