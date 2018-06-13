package ru.pavkin.telegram.api

import java.net.URLEncoder

import cats.effect.Effect
import cats.implicits._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Uri}
import ru.pavkin.telegram.api.dto.{BotResponse, BotUpdate}

import scala.language.higherKinds
import scala.util.Try

trait BotAPI[F[_], S[_]] {
  def sendMessage(chatId: Long, message: String): F[Unit]
  def pollUpdates(fromOffset: Long): S[BotUpdate]
}

trait StreamingBotAPI[F[_]] extends BotAPI[F, Stream[F, ?]]

case class Http4SBotAPI[F[_]](
  token: String,
  client: Client[F],
  logger: Logger[F])(
  implicit
  F: Effect[F],
  D: EntityDecoder[F, BotResponse[List[BotUpdate]]]) extends StreamingBotAPI[F] {

  def sendMessage(chatId: Long, message: String): F[Unit] = {
    val uri = Uri.fromString(
      s"https://api.telegram.org/bot$token/sendMessage?chat_id=$chatId&parse_mode=Markdown&text=${URLEncoder.encode(message, "UTF-8")}")
    for {
      u <- F.fromEither(uri)
      _ <- client.expect[Unit](u)
    } yield ()
  }

  def pollUpdates(fromOffset: Long): Stream[F, BotUpdate] = {
    Stream(()).repeat.covary[F]
      .evalMapAccumulate(fromOffset) { case (offset, _) =>
        val pollURI = Uri.fromString(
          s"https://api.telegram.org/bot$token/getUpdates?offset=${offset + 1}&timeout=0.5")
        val io = for {
          uri <- F.fromEither(pollURI)
          response <- client.expect[BotResponse[List[BotUpdate]]](uri)
        } yield (lastOffset(response).getOrElse(offset), response)
        io.recoverWith {
          case ex => logger.error(ex)("Failed to poll updates").as(offset -> BotResponse(ok = true, Nil))
        }
      }
      .flatMap { case (_, response) => Stream.emits(response.result) }
  }

  private def lastOffset(response: BotResponse[List[BotUpdate]]): Option[Long] =
    Try(response.result.maxBy(_.update_id).update_id).toOption
}
