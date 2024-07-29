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

object Header:
  def component(connect: String => IO[Unit]): Resource[IO, HtmlElement[IO]] = headerTag(
    h1("Scala WebRTC Chat"),
    for
      inputElem <- input(styleAttr := "width: 100%; margin-bottom: 10px;", placeholder := "Peer ID")
      onSubmitHandler =
        (ev: Event[IO]) =>
          for
            () <- ev.preventDefault
            id <- inputElem.value.get
            () <-
              if id.nonEmpty then
                connect(id)
              else
                IO.unit
            () <- inputElem.value.set("")
          yield ()
      formElem <- form(
        styleAttr := "width: 100%; display: flex; justify-content: space-between; align-items: center",
        onSubmit --> (_.foreach(onSubmitHandler)),
        inputElem,
        button(styleAttr := "margin-bottom: 10px;", `type` := "submit", "Connect")
      )
    yield formElem
  )
