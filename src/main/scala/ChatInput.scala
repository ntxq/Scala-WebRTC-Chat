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

object ChatInput:
  def component(messages: SignallingRef[IO, List[Signal[IO, Message]]]): Resource[IO, HtmlElement[IO]] =
    (input(styleAttr := "width: 90%;", placeholder := "Message")).flatMap { msgInput =>
      form(
        onSubmit -->
          (_.foreach(ev =>
            msgInput
              .value
              .get
              .flatMap(msg =>
                if msg.nonEmpty then
                  messages.update(Signal.constant(Message.SentMessage(msg)) :: _) >> msgInput.value.set("")
                else
                  IO.unit
              ) >> ev.preventDefault
          )),
        msgInput,
        button(styleAttr := "width: 10%;", `type` := "submit", "Send")
      )
    }
