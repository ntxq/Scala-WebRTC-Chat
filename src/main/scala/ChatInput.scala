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
  def component(onSendMessage: Message.SentMessage => IO[Unit]): Resource[IO, HtmlElement[IO]] =
    for
      inputBox <- (input(styleAttr := "width: 90%; margin-bottom: 0px;", placeholder := "Message"))
      onSubmitHandler =
        (ev: Event[IO]) =>
          for
            msg <- inputBox.value.get
            () <-
              if msg.nonEmpty then
                onSendMessage(Message.SentMessage(msg))
              else
                IO.unit
            () <- inputBox.value.set("")
            () <- ev.preventDefault
          yield ()
      form <- form(
        styleAttr := "margin-bottom: 0px;",
        onSubmit --> (_.foreach(onSubmitHandler)),
        inputBox,
        button(styleAttr := "width: 10%;", `type` := "submit", "Send")
      )
    yield form
