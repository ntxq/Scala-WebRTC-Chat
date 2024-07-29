import cats.*
import cats.data.*
import cats.effect.*
import cats.syntax.all.*

enum Message:
  case SentMessage(override val content: String) extends Message
  case ReceivedMessage(override val content: String) extends Message

  def content: String

object Message:
  given Hash[Message] = Hash[String].contramap(_.content)
