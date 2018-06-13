package ru.pavkin.telegram.api.dto

case class BotMessage(
  message_id: Long,
  chat: Chat,
  text: Option[String])
