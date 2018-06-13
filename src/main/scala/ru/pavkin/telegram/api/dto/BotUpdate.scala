package ru.pavkin.telegram.api.dto

case class BotUpdate(update_id: Long, message: Option[BotMessage])
