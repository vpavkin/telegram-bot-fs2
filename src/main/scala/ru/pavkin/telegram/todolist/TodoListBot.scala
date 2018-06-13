package ru.pavkin.telegram.todolist

import _root_.io.chrisdavenport.log4cats._
import cats.effect.Effect
import cats.implicits._
import fs2._
import ru.pavkin.telegram.todolist.BotCommand._
import ru.pavkin.telegram.api._

import scala.language.higherKinds
import scala.util.Random

case class TodoListBot[F[_]](
  api: BotAPI[F, Stream[F, ?]],
  storage: TodoListStorage[F],
  logger: Logger[F])(
  implicit F: Effect[F]) {

  def launch: Stream[F, Unit] = pollCommands.evalMap(handleCommand)

  def pollCommands: Stream[F, BotCommand] = for {
    update <- api.pollUpdates(0)
    pair <- Stream.emits(update.message.flatMap(a => a.text.map(a.chat.id -> _)).toSeq)
  } yield BotCommand.fromRawMessage(pair._1, pair._2)

  def handleCommand(command: BotCommand): F[Unit] = command match {
    case c: ClearTodoList => clearTodoList(c.chatId)
    case c: ShowTodoList => showTodoList(c.chatId)
    case c: AddEntry => addItem(c.chatId, c.content)
    case c: ShowHelp => api.sendMessage(c.chatId, List(
      "This bot stores your todo-list. Just write a task and the bot will store it! Other commands:",
      s"`$help` - show this help message",
      s"`$show` - show current todo-list",
      s"`$clear` - clear current list (vacation!)",
    ).mkString("\n"))
  }

  def clearTodoList(chatId: ChatId): F[Unit] = for {
    _ <- storage.clearList(chatId)
    _ <- (
      logger.info(s"todo list cleared for chat $chatId"),
      api.sendMessage(chatId, "Your todo-list was cleared!")
    ).tupled
  } yield ()

  def showTodoList(chatId: ChatId): F[Unit] = for {
    items <- storage.getItems(chatId)
    _ <- (
      logger.info(s"todo list queried for chat $chatId"),
      api.sendMessage(chatId,
        if (items.isEmpty) "You have no tasks planned!"
        else ("Your todo-list:" :: "" :: items.map(" - " + _)).mkString("\n"))
    ).tupled
  } yield ()

  def addItem(chatId: ChatId, item: String): F[Unit] = for {
    _ <- storage.addItem(chatId, item)
    response <- F.suspend(F.catchNonFatal(Random.shuffle(List("Ok!", "Sure!", "Noted", "Certainly!")).head))
    _ <- (
      logger.info(s"entry added for chat $chatId"),
      api.sendMessage(chatId, response)
    ).tupled
  } yield ()
}
