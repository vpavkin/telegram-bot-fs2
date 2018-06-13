package ru.pavkin.telegram.api.dto

case class BotResponse[T](ok: Boolean, result: T)
