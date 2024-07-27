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
  def component(messages: Signal[IO, List[Signal[IO, Message]]]): Resource[IO, HtmlElement[IO]] = div(
    styleAttr :=
      "height: 80%; overflow-y: auto; display: flex; flex-direction: column-reverse; gap: 2.5rem; padding: 2.5rem;",
    children <-- messages.map(_.traverse(Chatbox.component))
  )
