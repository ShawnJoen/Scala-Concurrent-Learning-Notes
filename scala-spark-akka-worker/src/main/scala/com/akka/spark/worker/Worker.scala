package com.akka.spark.worker

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Worker {
  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem("actorSystem", ConfigFactory.load())
    actorSystem.actorOf(Props(WorkerActor), "workerActor")
  }
}
