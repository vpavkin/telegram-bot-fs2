package ru.dm4x.dummy_tldr_bot.api.dto

case class BotResponse[T](ok: Boolean, result: T)
