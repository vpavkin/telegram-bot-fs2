package ru.pavkin.telegram.todolist

import cats.effect.Effect
import cats.implicits._
import fs2.Stream
import fs2.async.Ref
import io.chrisdavenport.log4cats.slf4j._
import org.http4s._
import org.http4s.client.blaze.Http1Client
import ru.pavkin.telegram.api.{ChatId, Http4SBotAPI}
import ru.pavkin.telegram.api.dto.{BotResponse, BotUpdate}

import scala.language.higherKinds

class TodoListBotProcess[F[_]](
  token: String)(
  implicit F: Effect[F],
  D: EntityDecoder[F, BotResponse[List[BotUpdate]]]) {

  def run: Stream[F, Unit] = Http1Client.stream[F]().flatMap { client =>
    val streamF = for {
      logger <- Slf4jLogger.create[F]
      storage <- Ref(Map.empty[ChatId, List[Item]]).map(InMemoryTodoListStorage(_))
      botAPI <- F.delay(Http4SBotAPI(token, client, logger))
      todoListBot <- F.delay(TodoListBot(botAPI, storage, logger))
    } yield todoListBot.launch

    Stream.force(streamF)
  }
}

