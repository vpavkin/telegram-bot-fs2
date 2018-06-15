package ru.pavkin.telegram.api

import java.net.URLEncoder

import cats.effect.Sync
import cats.implicits._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Uri}
import ru.pavkin.telegram.api.dto.{BotResponse, BotUpdate}

import scala.language.higherKinds
import scala.util.Try

/**
  * Simplified bot api algebra that exposes only APIs required for this project
  *
  * S is the streaming effect, see https://typelevel.org/blog/2018/05/09/tagless-final-streaming.html
  *
  * For the full API reference see https://core.telegram.org/bots/api
  */
trait BotAPI[F[_], S[_]] {
  /**
    * Send a message to specified chat
    */
  def sendMessage(chatId: ChatId, message: String): F[Unit]

  /**
    * Stream all updated for this bot using long polling. `S[_]` is the streaming effect.
    *
    * @param fromOffset offset of the fist message to start polling from
    */
  def pollUpdates(fromOffset: Offset): S[BotUpdate]
}

trait StreamingBotAPI[F[_]] extends BotAPI[F, Stream[F, ?]]

/**
  * Single bot API instance with http4s client.
  * Requires an implicit decoder for incoming bot updates.
  *
  * @param token  bot api token
  * @param client http client algebra
  * @param logger logger algebra
  */
case class Http4SBotAPI[F[_]](
  token: String,
  client: Client[F],
  logger: Logger[F])(
  implicit
  F: Sync[F],
  D: EntityDecoder[F, BotResponse[List[BotUpdate]]]) extends StreamingBotAPI[F] {

  def sendMessage(chatId: ChatId, message: String): F[Unit] = {
    val uri = Uri.fromString(
      s"https://api.telegram.org/bot$token/sendMessage?chat_id=$chatId&parse_mode=Markdown&text=${URLEncoder.encode(message, "UTF-8")}")

    for {
      u <- F.fromEither(uri)
      _ <- client.expect[Unit](u)
    } yield ()
  }

  def pollUpdates(fromOffset: Offset): Stream[F, BotUpdate] =
    Stream(()).repeat.covary[F]
      .evalMapAccumulate(fromOffset) { case (offset, _) => requestUpdates(offset) }
      .flatMap { case (_, response) => Stream.emits(response.result) }

  private def requestUpdates(offset: Offset): F[(Offset, BotResponse[List[BotUpdate]])] = {
    val pollURI = Uri.fromString(
      s"https://api.telegram.org/bot$token/getUpdates?offset=${offset + 1}&timeout=0.5")

    val requestIO = for {
      uri <- F.fromEither(pollURI)
      response <- client.expect[BotResponse[List[BotUpdate]]](uri)
    } yield (lastOffset(response).getOrElse(offset), response)

    requestIO.recoverWith {
      case ex => logger.error(ex)("Failed to poll updates").as(offset -> BotResponse(ok = true, Nil))
    }
  }

  private def lastOffset(response: BotResponse[List[BotUpdate]]): Option[Offset] =
    Try(response.result.maxBy(_.update_id).update_id).toOption
}
