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
import typings.peerjs.mod.Peer

object ScalaWebRTCChat extends IOWebApp:
  def resource =
    for
      title    <- SignallingRef[IO].of("Scala WebRTC Chat").toResource
      peer     <- SignallingRef[IO].of(Peer()).toResource
      messages <- SignallingRef[IO].of(List.empty[Signal[IO, Message]]).toResource
    yield (title, peer, messages)

  def render: Resource[IO, HtmlElement[IO]] = resource.flatMap { (title, peer, messages) =>
    val showId =
      for
        peer <- peer.get
        id   <- Async[IO].async_[String](k => peer.once[String]("open", id => k(Right(id.asInstanceOf[String]))))
        idMsg = Signal.constant[IO, Message](Message.ReceivedMessage(s"Your id is $id"))
        () <- messages.update(idMsg :: _)
      yield ()

    for
      _ <- showId.background
      html <- div(
        styleAttr := "height: 100%; display: flex; flex-direction: column;",
        Header.component(title),
        ChatList.component(messages),
        ChatInput.component(messages)
      )
    yield html
  }
