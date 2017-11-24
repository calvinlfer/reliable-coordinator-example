package actors

import akka.pattern.pipe
import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import models.{AResult, BResult, CResult, InitialInformation}
import services.Service
import services.ServiceA.InfoA
import services.ServiceB.InfoB
import services.ServiceC.InfoC

import scala.concurrent.{ExecutionContext, Future}

object ReliableCoordinator {
  sealed trait Command
  final case class StartBigTask(info: InitialInformation) extends Command

  sealed trait Response
  final case class TaskComplete(id: Int) extends Response

  sealed trait Event
  final case object ProcessStarted extends Event
  final case class FinishedTaskA(result: AResult) extends Event
  final case class FinishedTaskB(result: BResult) extends Event
  final case class FinishedTaskC(result: CResult) extends Event

  def props(serviceA: Service[InfoA, AResult], serviceB: Service[InfoB, BResult],
            serviceC: Service[InfoC, CResult]): Props = Props(new ReliableCoordinator(serviceA, serviceB, serviceC))
}

class ReliableCoordinator(serviceA: Service[InfoA, AResult], serviceB: Service[InfoB, BResult],
                          serviceC: Service[InfoC, CResult]) extends PersistentActor with ActorLogging {
  import actors.ReliableCoordinator._

  implicit val ec: ExecutionContext = context.dispatcher

  override def persistenceId = s"big-task-${self.path.name}"

  var replyTo: Option[ActorRef] = None

  override def receiveRecover: Receive = {
    // minimize scope of mutability
    var lastEvent: Event = ProcessStarted
    PartialFunction[Any, Unit] {
      case e: Event =>
        lastEvent = e

      case RecoveryCompleted =>
        // determine where we left off and resume
        lastEvent match {
          case ProcessStarted =>
            log.info("Tasks have not begun yet")

          case a: FinishedTaskA =>
            log.info("Task A has already been completed, resuming at Task B now")
            val aResult = a.result
            val infoB = (aResult.content, aResult.id)
            beginTaskB(infoB)

          case b: FinishedTaskB =>
            log.info("Task A and B have already been completed, resuming at Task C now")
            val bResult = b.result
            val infoC = (bResult.id, bResult.content.toString)
            beginTaskC(infoC)

          case _: FinishedTaskC =>
            log.info("Tasks A, B, and C have already been completed, stopping now")
            context stop self
        }
    }
  }

  override def receiveCommand: Receive = {
    case StartBigTask(info) =>
      // capture the requestor's ActorRef so we can reply to them
      replyTo = Some(sender())
      beginTaskA((info.time, info.content, info.id))
  }

  private def retryInfinitely[A, B](input: A, service: Service[A, B]): Future[B] =
    service.execute(input).recoverWith {
      case e: Exception =>
        log.error(e, s"service ${service.getClass.getSimpleName} failed to provide a response, retrying")
        retryInfinitely(input, service)
    }

  def beginTaskA(infoA: InfoA): Unit = {
    val result = retryInfinitely(infoA, serviceA)
    result.map(FinishedTaskA) pipeTo self
    context become awaitTaskA.orElse(recoverySenderCapture)
  }

  def awaitTaskA: Receive = {
    case event: FinishedTaskA =>
      persist(event) { finishedTaskA =>
        log.info("Task A has finished")
        val aResult = finishedTaskA.result
        val infoB: InfoB = (aResult.content, aResult.id)
        beginTaskB(infoB)
      }
  }

  def beginTaskB(infoB: InfoB): Unit = {
    val result = retryInfinitely(infoB, serviceB)
    result.map(FinishedTaskB) pipeTo self
    context become awaitTaskB.orElse(recoverySenderCapture)
  }

  def awaitTaskB: Receive = {
    case event: FinishedTaskB =>
      persist(event) { finishedTaskB =>
        log.info("Task B has finished")
        val bResult = finishedTaskB.result
        val infoC: InfoC = (bResult.id, bResult.content.toString)
        beginTaskC(infoC)
      }
  }

  def beginTaskC(infoC: InfoC): Unit = {
    val result = retryInfinitely(infoC, serviceC)
    result.map(FinishedTaskC) pipeTo self
    context become awaitTaskC.orElse(recoverySenderCapture)
  }

  def awaitTaskC: Receive = {
    case event: FinishedTaskC =>
      persist(event) { _ =>
        log.info("Task C has finished")
        log.info(s"$persistenceId has completed successfully!")

        replyTo.foreach { requestor =>
          log.info("sending message back to the requestor")
          requestor ! TaskComplete(event.result.id)
        }

        context stop self
      }
  }

  // In the recovery case, you will receive the initial command again
  // use the command to capture the sender
  def recoverySenderCapture: Receive = {
    case _: StartBigTask =>
      replyTo = Some(sender())
  }
}
