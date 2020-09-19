package com.mstruzek.dict.provider

import scala.language.postfixOps

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

object TestFut {

  def main(args: Array[String]): Unit = {

    var f0 = Future {
      Thread.sleep(10000)
      10
    }

    f0.foreach( ff => {
      println(ff)
    })

    var f1 = Future {
      Thread.sleep(1000)
      10
    }

    f1 = f1.flatMap(r1 => Future {
      r1 * 2
    })

    val f2 = Future {
      Thread.sleep(2000)
      20
    }

//    val eventualInt: Future[Int] = f1.flatMap(r1 => f2.map(r2 => r1 + r2))

    val eventualInt =
      for(
        r1 <- f1;
        r2 <- f2
      ) yield  {
        r1 + r2
      }

    Await.result(eventualInt, 10 seconds)

    println(eventualInt.value)

    f1.foreach( r1 => {
      println(r1)
    })

  }

}
