package services

import java.util.concurrent.ThreadLocalRandom

import models.CResult
import services.ServiceC.InfoC

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

object ServiceC {
  type IdB = Int
  type Content = String
  type InfoC = (IdB, Content)
}

class ServiceC extends Service[InfoC, CResult] {
  override def execute(input: InfoC)(implicit ec: ExecutionContext) = Future {
    val (idB, content) = input
    val diceRoll = ThreadLocalRandom.current().nextDouble()
    val delayTime = ThreadLocalRandom.current().nextLong(0, 1000)
    Thread sleep delayTime

    if (diceRoll >= 0.90) CResult(idB + 10, content.length.toDouble)
    else throw new Exception("Service C having trouble") with NoStackTrace
  }
}
