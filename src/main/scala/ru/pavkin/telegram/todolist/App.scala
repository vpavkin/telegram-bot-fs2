package ru.pavkin.telegram.todolist

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream

object App extends IOApp {

  def stream: Stream[IO, ExitCode] =
    for {
      token <- Stream.eval(IO(System.getenv("TODOLIST_BOT_TOKEN")))
      exitCode <-
        new TodoListBotProcess[IO](token).run.last.map(_ => ExitCode.Success)
    } yield exitCode

  override def run(args: List[String]): IO[ExitCode] =
    stream.compile.drain.as(ExitCode.Success)

}
