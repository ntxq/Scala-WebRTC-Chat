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

object ScalaWebRTCChat extends IOWebApp:
  def resource =
    for
      title <- SignallingRef[IO].of("Scala WebRTC Chat").toResource
      name <- SignallingRef[IO].of("World").toResource
      testMessage1 <- SignallingRef[IO]
        .of(Message.ReceivedMessage("This is my message."))
        .toResource
      testMessage2 <- SignallingRef[IO]
        .of(Message.SentMessage("This is your message."))
        .toResource
      messages <- SignallingRef[IO]
        .of(List(testMessage1, testMessage2))
        .toResource
    yield (title, name, messages)

  def render: Resource[IO, HtmlElement[IO]] =
    resource.flatMap { (title, name, messages) =>
      div(
        Header.component(title),
        ChatList.component(messages),
        label("Your name: "),
        input.withSelf { self =>
          (
            placeholder := "Enter your name here",
            onInput --> (_.foreach(_ => self.value.get.flatMap(name.set)))
          )
        },
        span(
          " Hello, ",
          name
        )
      )
    }
