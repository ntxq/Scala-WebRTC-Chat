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

object UserChatbox:
  def component(text: Signal[IO, String]): Resource[IO, HtmlElement[IO]] =
    div(
      styleAttr := "background-color: #1d7484; border-radius: 10%; width: fit-content; margin-left: auto;",
      p(
        styleAttr := "padding: 1em; color: white;",
        text
      )
    )
