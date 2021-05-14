package ru.pavkin.telegram.todolist

import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.Stream
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ru.pavkin.telegram.api.dto.{BotResponse, BotUpdate}
import ru.pavkin.telegram.api.{ChatId, Http4SBotAPI}

import scala.concurrent.ExecutionContext

/**
  * Creates and wires up everything that is needed to launch a [[TodoListBot]] and launches it.
  *
  * @param token telegram bot token
  */
class TodoListBotProcess[F[_]](token: String)(implicit F: ConcurrentEffect[F]) {

  implicit val decoder: EntityDecoder[F, BotResponse[List[BotUpdate]]] =
    jsonOf[F, BotResponse[List[BotUpdate]]]

  def run: Stream[F, Unit] =
    BlazeClientBuilder[F](ExecutionContext.global).stream.flatMap { client =>
      val streamF: F[Stream[F, Unit]] = for {
        logger <- Slf4jLogger.create[F]
        storage <-
          Ref
            .of(Map.empty[ChatId, List[Item]])
            .map(new InMemoryTodoListStorage(_))
        botAPI <- F.delay(new Http4SBotAPI(token, client, logger))
        todoListBot <- F.delay(new TodoListBot(botAPI, storage, logger))
      } yield todoListBot.launch

      Stream.force(streamF)
    }
}
