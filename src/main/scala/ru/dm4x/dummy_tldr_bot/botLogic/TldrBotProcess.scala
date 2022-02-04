package ru.dm4x.dummy_tldr_bot.botLogic

import cats.effect.Effect
import cats.implicits._
import fs2.Stream
import fs2.async.Ref
import io.chrisdavenport.log4cats.slf4j._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.blaze.Http1Client
import ru.dm4x.dummy_tldr_bot.api.dto.{BotResponse, BotUpdate}
import ru.dm4x.dummy_tldr_bot.api.{ChatId, Http4SBotAPI}

import scala.language.higherKinds

/**
  * Creates and wires up everything that is needed to launch a [[TldrBot]] and launches it.
  *
  * @param token telegram bot token
  */
class TldrBotProcess[F[_]](
  token: String)(
  implicit F: Effect[F]) {

  implicit val decoder: EntityDecoder[F, BotResponse[List[BotUpdate]]] = jsonOf[F, BotResponse[List[BotUpdate]]]

  def run: Stream[F, Unit] = Http1Client.stream[F]().flatMap { client =>
    val streamF = for {
      logger <- Slf4jLogger.create[F]
      storage <- Ref(List.empty[Item]).map(new InMemoryTodoListStorage(_))
      _ <- storage.fillStorage
      botAPI <- F.delay(new Http4SBotAPI(token, client, logger))
      tldrBot <- F.delay(new TldrBot(botAPI, storage, logger))
    } yield tldrBot.launch

    Stream.force(streamF)
  }
}

