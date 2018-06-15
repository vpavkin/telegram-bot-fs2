package ru.pavkin.telegram.todolist

import cats.effect.IO
import fs2.{Stream, StreamApp}

object App extends StreamApp[IO] {

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] = for {
    token <- Stream.eval(IO(System.getenv("TODOLIST_BOT_TOKEN")))
    exitCode <- new TodoListBotProcess[IO](token).run.last.map(_ => StreamApp.ExitCode.Success)
  } yield exitCode
}
