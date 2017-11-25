import java.time.ZonedDateTime

import actors.ReliableCoordinator
import actors.ReliableCoordinator.{StartBigTask, TaskComplete}
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.pattern.ask
import models.{AResult, BResult, CResult, InitialInformation}
import services.{Service, ServiceA, ServiceB, ServiceC}
import services.ServiceA.InfoA
import services.ServiceB.InfoB
import services.ServiceC.InfoC

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

object Main extends App {
  val system = ActorSystem("reliable-coordinator-system")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(1.minute)

  val serviceA: Service[InfoA, AResult] = new ServiceA
  val serviceB: Service[InfoB, BResult] = new ServiceB
  val serviceC: Service[InfoC, CResult] = new ServiceC

  val coordinator = system.actorOf(ReliableCoordinator.props(serviceA, serviceB, serviceC), "example-10")

  val result = (coordinator ? StartBigTask(InitialInformation(time = ZonedDateTime.now(), content = "hello", id = 1))).mapTo[TaskComplete]

  println {
    Await.result(result, 30.seconds)
  }

}
