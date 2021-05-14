package ru.pavkin.telegram.todolist

import cats.effect.Sync
import cats.implicits._
import fs2._
import org.typelevel.log4cats._
import ru.pavkin.telegram.api._
import ru.pavkin.telegram.todolist.BotCommand._

import scala.util.Random

/**
  * Todo list telegram bot
  * When launched, polls incoming commands and processes them using todo-list storage algebra.
  *
  * @param api     telegram bot api
  * @param storage storage algebra for todo-list items
  * @param logger  logger algebra
  */
class TodoListBot[F[_]](
  api: StreamingBotAPI[F],
  storage: TodoListStorage[F],
  logger: Logger[F])(
  implicit F: Sync[F]) {

  /**
    * Launches the bot process
    */
  def launch: Stream[F, Unit] = pollCommands.evalMap(handleCommand)

  private def pollCommands: Stream[F, BotCommand] = for {
    update <- api.pollUpdates(0)
    chatIdAndMessage <- Stream.emits(update.message.flatMap(a => a.text.map(a.chat.id -> _)).toSeq)
  } yield BotCommand.fromRawMessage(chatIdAndMessage._1, chatIdAndMessage._2)

  private def handleCommand(command: BotCommand): F[Unit] = command match {
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

  private def clearTodoList(chatId: ChatId): F[Unit] = for {
    _ <- storage.clearList(chatId)
    _ <- logger.info(s"todo list cleared for chat $chatId") *> api.sendMessage(chatId, "Your todo-list was cleared!")
  } yield ()

  private def showTodoList(chatId: ChatId): F[Unit] = for {
    items <- storage.getItems(chatId)
    _ <- logger.info(s"todo list queried for chat $chatId") *> api.sendMessage(chatId,
      if (items.isEmpty) "You have no tasks planned!"
      else ("Your todo-list:" :: "" :: items.map(" - " + _)).mkString("\n"))
  } yield ()

  private def addItem(chatId: ChatId, item: Item): F[Unit] = for {
    _ <- storage.addItem(chatId, item)
    response <- F.defer(F.catchNonFatal(Random.shuffle(List("Ok!", "Sure!", "Noted", "Certainly!")).head))
    _ <- logger.info(s"entry added for chat $chatId") *> api.sendMessage(chatId, response)
  } yield ()
}
