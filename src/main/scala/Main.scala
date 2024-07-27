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
    yield (title, name)

  def render: Resource[IO, HtmlElement[IO]] =
    resource.flatMap { (title, name) =>
      div(
        Header.component(title),
        OpponentChatbox.component(Signal.constant("This is my choxtbox.")),
        UserChatbox.component(Signal.constant("This is your chatbox.")),
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
