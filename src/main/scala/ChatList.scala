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

object ChatList:
  def component(
      messages: Signal[IO, List[SignallingRef[IO, Message]]]
  ): Resource[IO, HtmlElement[IO]] =
    div(
      children <-- messages.map(_.traverse(Chatbox.component))
    )
