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
    for rtcService <- RTCService.init
    yield rtcService

  def render: Resource[IO, HtmlElement[IO]] = resource.flatMap { rtcService =>
    div(
      styleAttr := "height: 100%; display: flex; flex-direction: column;",
      Header.component(rtcService.connect),
      ChatList.component(rtcService.messages),
      ChatInput.component(rtcService.sendMessage)
    )
  }
