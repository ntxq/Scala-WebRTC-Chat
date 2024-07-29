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
import scalajs.js
import typings.peerjs.mod.DataConnection
import typings.peerjs.mod.Peer

trait RTCService:
  def connect(id: String): IO[Unit]
  def sendMessage(message: Message.SentMessage): IO[Unit]
  def messages: Signal[IO, List[Signal[IO, Message]]]

object RTCService:
  def init: Resource[IO, RTCService] =
    def showPeerID(peer: Peer, messageSignal: SignallingRef[IO, List[Signal[IO, Message]]]) =
      (
        for
          id <- IO.async_[String](k => peer.once("open", id => k(Right(id.asInstanceOf[String]))))
          () <- messageSignal.update(Signal.constant[IO, Message](Message.ReceivedMessage(s"Your id is $id")) :: _)
        yield ()
      ).background.void

    def acceptConnections(peer: Peer, connectionQueue: Queue[IO, DataConnection]) =
      (for
        dispatcher <- Dispatcher.sequential[IO]
        _ <-
          IO {
            def accept(conn: DataConnection): Unit = dispatcher.unsafeRunAndForget(connectionQueue.offer(conn))
            peer.on("connection", conn => accept(conn.asInstanceOf[DataConnection]))
          }.toResource
      yield ())

    def sendMessages(
        sendQueue: Queue[IO, Message.SentMessage],
        connectionSignal: SignallingRef[IO, List[DataConnection]],
        messageSignal: SignallingRef[IO, List[Signal[IO, Message]]]
    ) =
      Stream
        .fromQueueUnterminated(sendQueue)
        .flatMap { toSend =>
          val addToMessageSignal = messageSignal.update(Signal.constant(toSend) :: _)
          val sendToAllConnections = Stream
            .evalSeq(connectionSignal.get)
            .parEvalMapUnorderedUnbounded { conn =>
              conn.send(toSend.content) match
                case _: Unit =>
                  IO.unit
                case p: js.Promise[Unit] =>
                  IO.fromPromise(IO(p))
            }

          Stream.eval(addToMessageSignal) merge sendToAllConnections
        }
        .compile
        .drain
        .background
        .void

    def receiveMessages(
        connectionQueue: Queue[IO, DataConnection],
        connectionSignal: SignallingRef[IO, List[DataConnection]],
        messageSignal: SignallingRef[IO, List[Signal[IO, Message]]]
    ) =
      (
        for
          dispatcher <- Stream.resource(Dispatcher.sequential[IO])
          conn       <- Stream.fromQueueUnterminated(connectionQueue)
          addToConnectionSignal = connectionSignal.update(conn :: _)
          attachDataListener = IO {
            def accept(v: Any): Unit = dispatcher.unsafeRunAndForget(
              messageSignal.update(Signal.constant(Message.ReceivedMessage(v.asInstanceOf[String])) :: _)
            )
            conn.on("data", data => accept(data))
          }
          () <- Stream.eval(IO.racePair(addToConnectionSignal, attachDataListener).void)
        yield ()
      ).compile.drain.background.void

    for
      peer             <- Resource.make(IO(Peer()))(peer => IO(peer.destroy()))
      connectionQueue  <- Queue.unbounded[IO, DataConnection].toResource
      sendQueue        <- Queue.unbounded[IO, Message.SentMessage].toResource
      messageSignal    <- SignallingRef[IO].of(List.empty[Signal[IO, Message]]).toResource
      connectionSignal <- SignallingRef[IO].of(List.empty[DataConnection]).toResource
      ()               <- showPeerID(peer, messageSignal)
      ()               <- acceptConnections(peer, connectionQueue)
      ()               <- sendMessages(sendQueue, connectionSignal, messageSignal)
      ()               <- receiveMessages(connectionQueue, connectionSignal, messageSignal)
    yield new RTCService:
      def connect(id: String): IO[Unit] =
        for
          conn <- IO(peer.connect(id))
          ()   <- IO.async_[Unit](k => conn.once("open", _ => k(Right(()))))
          ()   <- connectionQueue.offer(conn)
        yield ()

      def sendMessage(message: SentMessage): IO[Unit] = sendQueue.offer(message)

      def messages: Signal[IO, List[Signal[IO, Message]]] = messageSignal
