package services

import java.util.concurrent.ThreadLocalRandom

import models.BResult
import services.ServiceB.InfoB

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

object ServiceB {
  type ContentA = String
  type IdA = Int
  type InfoB = (ContentA, IdA)
}

class ServiceB extends Service[InfoB, BResult] {
  override def execute(input: InfoB)(implicit ec: ExecutionContext) = Future {
    val (_, idA) = input
    val diceRoll = ThreadLocalRandom.current().nextDouble()
    val delayTime = ThreadLocalRandom.current().nextLong(0, 1000)
    Thread sleep delayTime

    if (diceRoll >= 0.80) BResult(idA + 10, idA.toFloat)
    else throw new Exception("Service B having trouble") with NoStackTrace
  }
}
