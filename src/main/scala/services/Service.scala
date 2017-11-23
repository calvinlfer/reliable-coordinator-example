package services

import scala.concurrent.{ExecutionContext, Future}

trait Service[I, O] {
  def execute(input: I)(implicit ec: ExecutionContext): Future[O]
}
