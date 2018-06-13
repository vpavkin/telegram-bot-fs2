package ru.pavkin.telegram.todolist

import ru.pavkin.telegram.api.ChatId

sealed trait BotCommand

object BotCommand {

  case class ShowHelp(chatId: ChatId) extends BotCommand
  case class ClearTodoList(chatId: ChatId) extends BotCommand
  case class ShowTodoList(chatId: ChatId) extends BotCommand
  case class AddEntry(chatId: ChatId, content: String) extends BotCommand

  def fromRawMessage(chatId: ChatId, message: String): BotCommand = message match {
    case `help` | "/start" => ShowHelp(chatId)
    case `show` => ShowTodoList(chatId)
    case `clear` => ClearTodoList(chatId)
    case _ => AddEntry(chatId, message)
  }

  val help = "?"
  val show = "/show"
  val clear = "/clear"
}
