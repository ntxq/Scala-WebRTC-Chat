import Message.*
import calico.*
import calico.html.io.*
import calico.html.io.given
import calico.syntax.*
import cats.*
import cats.data.*
import cats.effect.*
import cats.syntax.all.*
import fs2.*
import fs2.concurrent.*
import fs2.dom.*

object Chatbox:
  def component(message: Signal[IO, Message]): Resource[IO, HtmlElement[IO]] = message
    .get
    .toResource
    .flatMap {
      case SentMessage(content) =>
        sentChatbox(message)
      case ReceivedMessage(content) =>
        receivedChatbox(message)
    }

  def receivedChatbox(message: Signal[IO, Message]): Resource[IO, HtmlElement[IO]] = div(
    styleAttr := "background-color: #f1f1f1; border-radius: 15px; width: fit-content;",
    p(styleAttr := "padding: 1em;", message.map(_.content))
  )

  def sentChatbox(message: Signal[IO, Message]): Resource[IO, HtmlElement[IO]] = div(
    styleAttr := "background-color: #1d7484; border-radius: 15px; width: fit-content; margin-left: auto;",
    p(styleAttr := "padding: 1em; color: white;", message.map(_.content))
  )
