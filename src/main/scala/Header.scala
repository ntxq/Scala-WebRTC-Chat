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
    styleAttr := "display: flex; justify-content: space-between; align-items: center;",
    h1("Scala WebRTC Chat"),
    button(
      styleAttr := "margin: 30px 0 15px 0;",
      `type`    := "button",
      onClick --> (_.foreach(_ => connect("c1d18719-358e-4110-9db0-f42083cd6675"))),
      "Connect"
    )
  )
