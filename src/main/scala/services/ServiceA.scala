package services

import java.time.ZonedDateTime
import java.util.concurrent.ThreadLocalRandom

import models.AResult
import services.ServiceA.{Content, Id, InfoA, Time}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

object ServiceA {
  type Time = ZonedDateTime
  type Content = String
  type Id = Int
  type InfoA = (Time, Content, Id)
}

class ServiceA extends Service[InfoA, AResult] {
  override def execute(input: (Time, Content, Id))(implicit ec: ExecutionContext) = Future {
    val (_, content, id) = input
    val diceRoll = ThreadLocalRandom.current().nextDouble()
    val delayTime = ThreadLocalRandom.current().nextLong(0, 1000)
    Thread sleep delayTime

    if (diceRoll >= 0.70) AResult(id * 1000, content.reverse)
    else throw new Exception("Service A having trouble") with NoStackTrace
  }
}
