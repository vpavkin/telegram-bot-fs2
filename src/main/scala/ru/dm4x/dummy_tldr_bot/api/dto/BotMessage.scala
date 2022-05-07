package ru.dm4x.dummy_tldr_bot.api.dto

import ru.dm4x.dummy_tldr_bot.api.Date

case class BotMessage(
  message_id: Long,
  from: From,
  chat: Chat,
  date: Date,
  text: Option[String])
