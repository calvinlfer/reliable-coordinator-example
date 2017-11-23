import java.time.ZonedDateTime

package object models {
  final case class InitialInformation(time: ZonedDateTime, content: String, id: Int)

  sealed trait Result { def id: Int }
  final case class AResult(id: Int, content: String) extends Result
  final case class BResult(id: Int, content: Float) extends Result
  final case class CResult(id: Int, content: Double) extends Result
}
