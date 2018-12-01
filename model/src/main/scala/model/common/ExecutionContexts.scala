package model.common

import cats.effect.IO
import cats.effect.IO.fromFuture

object ExecutionContexts {
  implicit def futureToTask[A](fut: => scala.concurrent.Future[A]): IO[A] = fromFuture(IO(fut))
}
