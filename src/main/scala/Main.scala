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
      title    <- SignallingRef[IO].of("Scala WebRTC Chat").toResource
      name     <- SignallingRef[IO].of("World").toResource
      messages <- SignallingRef[IO].of(List.empty[Signal[IO, Message]]).toResource
    yield (title, name, messages)

  def render: Resource[IO, HtmlElement[IO]] = resource.flatMap { (title, name, messages) =>
    div(Header.component(title), ChatList.component(messages), ChatInput.component(messages))
  }
