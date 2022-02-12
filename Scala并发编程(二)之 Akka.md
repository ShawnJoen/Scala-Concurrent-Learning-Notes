# 概述
- Akka是使用 Scala开发的库, 它是基于事件驱动的, 用于构建高并发项目的工具包 
> - Akka特性:
> 1) 提供基于异步非阻塞, 高性能的事件驱动编程模型
> 2) 内置容错机制, 允许 Actor出错时, 进行恢复或重置
> 3) 轻量级的事件处理(每 GB堆内存几百万 Actor. *`轻量级事件处理和重量级的划分, 主要看是否依赖操作系统和硬件, 依赖是重量级, 不依赖是轻量级`
> 4) 可在单机上构建高并发应用, 也可在网络中构建分布式应用

## Akka通信过程

![image](https://github.com/ShawnJoen/Scala-Concurrent-Learning-Notes/blob/main/images/1.png)

1. 学生创建一个 ActorSystem
2. 通过 ActorSystem来创建一个 ActorRef(老师的引用), 并将消息发送给 ActorRef
3. ActorRef将消息发送给 Message Dispatcher(消息分发器)
4. Message Dispatcher将消息按照顺序保存到目标 Actor的 MailBox中
5. Message Dispatcher将 MailBox放到一个线程中
6. MailBox按照顺序取出消息, 最终将它递给 TeacherActor接收的方法中

> API介绍: 
> * ActorSystem: 负责创建和监督 Actor
> 1. ActorSystem是一个单例对象, 通过它可创建很多 Actor
> 2. 使用 `context.system`可以获取到管理该 Actor的 ActorSystem的引用
> * 实现 Actor类
> 1. 定义类或单例对象继承 Actor(`import akka.actor.Actor`)
> 2. 实现 receive方法接收消息(无需加 loop& react方法)
> 3. 可以实现 preStart()方法(可选), 该方法在 Actor对象构建后执行, 在 Actor生命周期中仅执行一次
> * 加载 Actor
> 1. 要创建 Akka的 Actor对象, 必须先创建 ActorSystem
> 2. 调用 ActorSystem.actorOf(Props(Actor对象), "Actor名称")来加载 Actor

## Actor Path
- 每一个 Actor都有一个 Path, 这个路径可以被外部引用
```
类型 | 路径
--|:--:
本地 Actor | akka://actorSystem名称/user/Actor名称
远程 Actor | akka.tcp://dest-actorSystem@ip地址:port/user/Actor名称
```
# 入门案例
- 通过 ActorSystem加载两个 Actor(SenderActor& ReceiverActor), 并从 SenderActor发消息, 在 ReceiverActor接收, 再回复消息

![image](https://github.com/ShawnJoen/Scala-Concurrent-Learning-Notes/blob/main/images/2.png)

```
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


package com.akka.ex1

import akka.actor.Actor

object SenderActor extends Actor {
  override def receive: Receive = {
    // 接收 Entrance发的消息: start
    case "start" => {
      println("SenderActor, received: start!")
      // 获取 ReceiverActor的路径
      val receiverActor = context.actorSelection("akka://actorSystem/user/receiverActor")
      // 给 ReceiverActor发送消息
      receiverActor ! SubmitTaskMessage("Hello ReceiverActor!, This is SenderActor.")
    }
    // 接收 ReceiverActor返回的回执信息
    case SuccessSubmitTaskMessage(msg) => println(s"SenderActor, received: ${msg}")
  }
}


package com.akka.ex1

import akka.actor.Actor

object ReceiverActor extends Actor {
  override def receive: Receive = {
    // 接收 SenderActor发的消息
    case SubmitTaskMessage(msg) => {
      println(s"ReceiverActor, received: ${msg}")
      // 给 SenderActor回复信息
      sender ! SuccessSubmitTaskMessage("Hi!, This is ReceiverActor.")
    }
  }
}


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
SenderActor, received: start!
ReceiverActor, received: Hello ReceiverActor!, This is SenderActor.
SenderActor, received: Hi!, This is ReceiverActor.

```

# 定时任务案例
- 通过 `ActorSystem.scheduler.schedule()方法`, 启动定时任务
- 使用方式 1:
```

	final def schedule(
		initialDelay : FiniteDuration, // 首次开始, 按此设定的时间, 延迟后执行
		interval : FiniteDuration, // 每隔多久执行一次(首次开始, 立马执行, 不延时
		receiver : ActorRef, // 设置目标接收消息的 Actor
		message : Any) // 要发送的消息
	(implicit executor : ExecutionContext, sender : ActorRef = {}) // 隐式参数, 需导入

```
- 使用方式 2:
```

	final def schedule(
		initialDelay : FiniteDuration, // 首次开始, 按此设定的时间, 延迟后执行
		interval : FiniteDuration // 每隔多久执行一次(首次开始, 立马执行, 不延时
	)(f : => Unit) // 定期要执行的函数(消息
	(implicit executor : ExecutionContext) // 隐式参数, 需导入

```

```
package com.akka.ex2

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object MainActor {
  object ReceiverActor extends Actor {
    override def receive: Receive = {
      case x => println(x)
    }
  }

  def main(args: Array[String]): Unit = {
    // 创建ActorSystem, 用来负责创建和监督 Actor
    val actorSystem = ActorSystem("actorSystem", ConfigFactory.load())
    // 通过 ActorSystem来加载自定义 Actor对象
    val receiverActor = actorSystem.actorOf(Props(ReceiverActor), "receiverActor")

    // 导入隐式参数& 转换
    import actorSystem.dispatcher
    import scala.concurrent.duration._

    // 通过定时器, 定时给 ReceiverActor发送消息
    // 方式 1: 采用提供的 Any数据类型参数的消息
    actorSystem.scheduler.schedule(3 seconds, 2 seconds, receiverActor, "Hello ReceiverActor!, 111.")

    // 方式 2: 采用自定义函数的消息
    actorSystem.scheduler.schedule(0 seconds, 5 seconds) {
      receiverActor ! "Hello ReceiverActor!, 222."
    }
  }
}

```

# 两个进程之间的通信案例

![image](https://github.com/ShawnJoen/Scala-Concurrent-Learning-Notes/blob/main/images/3.png)

1. WorkerActor发送 "connect"消息给 MasterActor
2. MasterActor回复 "success"消息给 WorkerActor
3. WorkerActor接收并打印接收到的消息
```
package com.akka.master

import akka.actor.Actor

object MasterActor extends Actor {
  override def receive: Receive = {
    case "setup" => println("MasterActor started!")
    // 接收 WorkerActor发的消息
    case "connect" => {
      println("MasterActor, received: connect!")
      // 给发送者(WorkerActor)返回的回执信息
      sender ! "success"
    }
  }
}

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


package com.akka.worker

import akka.actor.Actor

// WorkerActor的路径: akka.tcp://actorSystem@127.0.0.1:8081/user/workerActor
object WorkerActor extends Actor {
  override def receive: Receive = {
    case "setup" => {
      println("WorkerActor started!")
      // 远程获取 MasterActor
      val masterActor = context.system.actorSelection("akka.tcp://actorSystem@127.0.0.1:8080/user/masterActor")
      // 给 MasterActor发送字符串 connect
      masterActor ! "connect"
    }
    // 接收 MasterActor发的消息
    case "success" => println("MasterActor, received: success!")
  }
}

package com.akka.worker

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Entrance {
  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem("actorSystem", ConfigFactory.load())
    val workerActor = actorSystem.actorOf(Props(WorkerActor), "workerActor")
    // 给 WorkerActor发送消息
    workerActor ! "setup"
  }
}

```

# 简易版 Spark通信框架实现案例
- 模拟 Spark的 Master与多个 Worker的通信

![image](https://github.com/ShawnJoen/Scala-Concurrent-Learning-Notes/blob/main/images/4.png)

> - 运作步骤:
>
> (1) 启动 MasterActor
>> 1.1.) MasterActor对象构建后, `开启定时任务(用于自检, 为移除超时的 WorkerActor`
>
> (2) 启动 WorkerActor
>> 2.1.) WorkerActor对象构建后, 将自身信息封装成`注册信息`后, 发给 MasterActor
>
> (3) MasterActor接收 WorkerActor的注册信息, 并保存
>> 3.1.) 给 WorkerActor回执信息
>
> (4) WorkerActor请求注册后, 接收到信息, 并打印 Connection is successful!
>> 4.1.) 开启定时任务, 给 MasterActor发心跳消息
>
> (5) MasterActor接收 WorkerActor发来的心跳消息, 将该 WorkerActor的注册信息中的最后心跳时间更新为当前时间
>
```
工程名 | 说明
--|:--:
scala-spark-akka-common | 存放公共的消息实体类
scala-spark-akka-master | Akka Master节点
scala-spark-akka-worker | Akka Worker节点
```
```
package com.akka.spark.common

/**
 * 用来保存已注册的 WorkerActor的信息的类
 *
 * @param workerId : WorkerActor的 Id(UUID
 * @param cpuCores : WorkerActor的 CPU核数
 * @param memory : WorkerActor 的内存大小
 * @param lastHeartBeatTime : 最后一次心跳时间
 */
case class WorkerInfo(workerId: String, cpuCores: Int, memory: Int, lastHeartBeatTime: Long)


package com.akka.spark.common

/**
 * WorkerActor提交注册信息的类
 *
 * @param workerId : WorkerActor的 Id(UUID
 * @param cpuCores : WorkerActor的 CPU核数
 * @param memory : WorkerActor 的内存大小
 */
case class WorkerRegisterMessage(workerId: String, cpuCores: Int, memory: Int)

/** 注册成功后回执的单例对象*/
case object RegisterSuccessMessage

/**
 * WorkerActor定时触发心跳到 MasterActor的信息类
 *
 * @param workerId : WorkerActor的 Id(UUID
 * @param cpuCores : WorkerActor的 CPU核数
 * @param memory : WorkerActor 的内存大小
 */
case class WorkerHeartBeatMessage(workerId: String, cpuCores: Int, memory: Int)


package com.akka.spark.master

import com.typesafe.config.{Config, ConfigFactory}

// 用来读取配置文件信息的类
object ConfigUtils {
  // 1. 获取配置文件对象
  private val config: Config = ConfigFactory.load()
  // 2. 获取检查 WorkerActor心跳的时间间隔
  val `master.check.heartbeat.interval` = config.getInt("master.check.heartbeat.interval")
  // 3. 获取 WorkerActor心跳超时时间
  val `master.check.heartbeat.timeout` = config.getInt("master.check.heartbeat.timeout")
}


package com.akka.spark.master

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Master {
  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem("actorSystem", ConfigFactory.load())
    actorSystem.actorOf(Props(MasterActor), "masterActor")
  }
}


package com.akka.spark.master

import java.util.Date
import akka.actor.Actor
import com.akka.spark.common.{RegisterSuccessMessage, WorkerHeartBeatMessage, WorkerInfo, WorkerRegisterMessage}

object MasterActor extends Actor {
  // 1. 定义一个可变的 Map集合, 用来保存已注册的 WorkerActor信息
  private val regWorkerMap = scala.collection.mutable.Map[String, WorkerInfo]()

  // MasterActor定期检查 WorkerActor心跳, 将超时的 Worker移除
  override def preStart(): Unit = {
    // 1. 导入时间隐式参数& 转换
    import context.dispatcher
    import scala.concurrent.duration._

    // 2. 启动定时任务(MasterActor自检移除超时的 WorkerActor)
    context.system.scheduler.schedule(0 seconds, ConfigUtils.`master.check.heartbeat.interval` seconds) {
      // 3. 过滤超时的 WorkerActor(返回被过滤的 workerId集合
      val timeOutWorkerMap = regWorkerMap.filter {
        keyVal => // keyVal的数据格式: workerId -> WorkerInfo(workerId, cpuCores, memory, lastHeartBeatTime)
          // 3.1 获取当前 WorkerActor对象的最后一次心跳时间
          val lastHeartBeatTime = keyVal._2.lastHeartBeatTime
          // 3.2 如果超时, 则 true, 否则 false (当前时间 - 最后一次心跳时间) > 最大超时时间 * 1000
          if ((new Date().getTime - lastHeartBeatTime) > (ConfigUtils.`master.check.heartbeat.timeout` * 1000)) true else false
      }

      // 4. 将要移除的已超时 workerId集合
      if (!timeOutWorkerMap.isEmpty) {
        regWorkerMap --= timeOutWorkerMap.map(_._1) // ArrayBuffer(5b9feb50-5c33-496b-a325-dd168360281b)
      }

      // 5. 有效的 WorkerActor, 按内存大小进行降序排列
      val workerList = regWorkerMap.map(_._2).toList.sortBy(_.memory).reverse
      println(s"Active WorkerActors: ${workerList}")
    }
  }

  override def receive: Receive = {
    // 接收 WorkerActor的注册信息
    case WorkerRegisterMessage(workerId, cpuCores, memory) => {
      // 打印接收到的注册信息
      println(s"MasterActor, received info: ${workerId}, ${cpuCores}, ${memory}")

      // 将注册信息保存到哈希表中& 并记录最后一次心跳时间
      regWorkerMap += workerId -> WorkerInfo(workerId, cpuCores, memory, new Date().getTime)

      // 给注册的 WorkerActor回执信息
      sender ! RegisterSuccessMessage
    }

    // 接收 WorkerActor发来的心跳消息
    case WorkerHeartBeatMessage(workerId, cpuCores, memory) => {
      println(s"MasterActor, received heartbeat: ${workerId}")
      // 更新指定的 WorkerActor对象的最后一次心跳时间
      regWorkerMap += workerId -> WorkerInfo(workerId, cpuCores, memory, new Date().getTime)
    }
  }
}


package com.akka.spark.worker

import com.typesafe.config.{Config, ConfigFactory}

// 用来读取配置文件信息的类
object ConfigUtils {
  // 1. 获取配置文件对象
  private val config: Config = ConfigFactory.load()
  // 2. 获取 WorkerActor心跳的间隔时间
  val `worker.heartbeat.interval`: Int = config.getInt("worker.heartbeat.interval")
}


package com.akka.spark.worker

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Worker {
  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem("actorSystem", ConfigFactory.load())
    actorSystem.actorOf(Props(WorkerActor), "workerActor")
  }
}


package com.akka.spark.worker

import java.util.UUID
import akka.actor.{Actor, ActorSelection}
import com.akka.spark.common.{RegisterSuccessMessage, WorkerHeartBeatMessage, WorkerRegisterMessage}
import scala.util.Random

object WorkerActor extends Actor {
  // 表示 MasterActor的引用
  private var masterActor: ActorSelection = _
  // WorkerActor的注册信息
  private var workerId: String = _
  private var cpuCores: Int = _ // CPU核数
  private var memory: Int = _ // 内存大小
  private val cpuCoreList = List(1, 2, 3, 4, 6, 8) // CPU核心数的随机取值范围
  private val memoryList = List(512, 1024, 2048, 4096) // 内存大小的随机取值范围

  override def preStart(): Unit = {
    // 获取 MasterActor的引用
    masterActor = context.system.actorSelection("akka.tcp://actorSystem@127.0.0.1:8080/user/masterActor")

    // 随机设置编号
    workerId = UUID.randomUUID().toString

    // 随机选择 WorkerActor的 CPU核数和内存大小
    val r = new Random()
    cpuCores = cpuCoreList(r.nextInt(cpuCoreList.length))
    memory = memoryList(r.nextInt(memoryList.length))

    // 封装 WorkerActor的注册信息
    val registerMessage = WorkerRegisterMessage(workerId, cpuCores, memory)
    // 发给 MasterActor
    masterActor ! registerMessage
  }

  override def receive: Receive = {
    // 注册成功的回执信息
    case RegisterSuccessMessage => {
      println("Connection is successful!")

      // 1. 导入时间隐式参数& 转换
      import context.dispatcher
      import scala.concurrent.duration._

      // 定时给 MasterActor发心跳消息
      context.system.scheduler.schedule(0 seconds, ConfigUtils.`worker.heartbeat.interval` seconds) {
        masterActor ! WorkerHeartBeatMessage(workerId, cpuCores, memory)
      }
    }
  }
}

```