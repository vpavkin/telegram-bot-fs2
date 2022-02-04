package ru.dm4x.dummy_tldr_bot.botLogic

import ru.dm4x.dummy_tldr_bot.api.ChatId

sealed trait BotCommand

object BotCommand {

  case class ShowHelp(chatId: ChatId) extends BotCommand
  case class Tldr(chatId: ChatId) extends BotCommand
  case class SilentWatcher(chatId: ChatId) extends BotCommand

  def fromRawMessage(chatId: ChatId, message: String): BotCommand = message match {
    case `help` | "/start" => ShowHelp(chatId)
    case `tldr` => Tldr(chatId)
    case _ => SilentWatcher(chatId)
  }

  val help = "/help"
  val tldr = "/tldr"
}
