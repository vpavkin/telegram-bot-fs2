package ru.dm4x.dummy_tldr_bot.api.dto

case class BotMessage(
  message_id: Long,
  chat: Chat,
  text: Option[String])
