import Message.SentMessage
import calico.*
import calico.html.io.*
import calico.html.io.given
import calico.syntax.*
import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.std.Dispatcher
import cats.effect.std.MapRef
import cats.effect.std.Queue
import cats.syntax.all.*
import fs2.*
import fs2.concurrent.*
import fs2.dom.*
import scalajs.js.Promise
import typings.peerjs.mod.DataConnection
import typings.peerjs.mod.Peer

trait RTCService:
  def connect(id: String): IO[Unit]
  def sendMessage(message: Message.SentMessage): IO[Unit]
  def messages: Signal[IO, List[Signal[IO, Message]]]

object RTCService:
  def init: Resource[IO, RTCService] =
    for
      peer          <- Resource.make(IO(Peer()))(peer => IO(peer.destroy()))
      connections   <- Queue.unbounded[IO, DataConnection].toResource
      sendQueue     <- Queue.unbounded[IO, Message.SentMessage].toResource
      messageSignal <- SignallingRef[IO].of(List.empty[Signal[IO, Message]]).toResource
      _ <-
        (
          for
            id <- IO.async_[String](k => peer.once("open", id => k(Right(id.asInstanceOf[String]))))
            () <- messageSignal.update(Signal.constant[IO, Message](Message.ReceivedMessage(s"Your id is $id")) :: _)
          yield ()
        ).background
    yield new RTCService:
      def connect(id: String): IO[Unit] =
        for
          conn <- IO(peer.connect(id))
          ()   <- connections.offer(conn)
        yield ()

      def sendMessage(message: SentMessage): IO[Unit] = sendQueue.offer(message)

      def messages: Signal[IO, List[Signal[IO, Message]]] = messageSignal
