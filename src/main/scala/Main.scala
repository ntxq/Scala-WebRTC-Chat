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
  def render: Resource[IO, HtmlElement[IO]] =
    SignallingRef[IO].of("world").toResource.flatMap { name =>
      div(
        label("Your name: "),
        input.withSelf { self =>
          (
            placeholder := "Enter your name here",
            onInput --> (_.foreach(_ => self.value.get.flatMap(name.set)))
          )
        },
        span(
          " Hello, ",
          name.map(_.toUpperCase)
        )
      )
    }
