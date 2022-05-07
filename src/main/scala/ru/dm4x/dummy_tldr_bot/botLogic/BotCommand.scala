package ru.dm4x.dummy_tldr_bot.botLogic

import ru.dm4x.dummy_tldr_bot.api.ChatId
import ru.dm4x.dummy_tldr_bot.api.dto.BotMessage

sealed trait BotCommand

object BotCommand {

  case class ShowHelp(chatId: ChatId) extends BotCommand
  case class Tldr(chatId: ChatId) extends BotCommand
  case class SilentWatcher(message: BotMessage) extends BotCommand

  def fromRawMessage(message: BotMessage): BotCommand = message.text match {
    case Some(`help`) | Some("/start") => ShowHelp(message.chat.id)
    case Some(`tldr`) => Tldr(message.chat.id)
    case _ => SilentWatcher(message)
  }

  val help = "/help"
  val tldr = "/tldr"
}
