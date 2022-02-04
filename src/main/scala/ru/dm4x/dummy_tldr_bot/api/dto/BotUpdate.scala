package ru.dm4x.dummy_tldr_bot.api.dto

case class BotUpdate(update_id: Long, message: Option[BotMessage])
