package ru.pavkin.telegram.todolist

import cats.effect.IO
import fs2.{Stream, StreamApp}
import io.circe.generic.auto._
import org.http4s.circe._
import ru.pavkin.telegram.api.dto.{BotResponse, BotUpdate}

object App extends StreamApp[IO] {

  implicit val decoder = jsonOf[IO, BotResponse[List[BotUpdate]]]

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] = for {
    token <- Stream.eval(IO(System.getenv("TODOLIST_BOT_TOKEN")))
    exitCode <- new TodoListBotProcess[IO](token).run.last.map(_ => StreamApp.ExitCode.Success)
  } yield exitCode
}
