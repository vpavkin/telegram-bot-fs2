package ru.dm4x.dummy_tldr_bot.api.dto

import ru.dm4x.dummy_tldr_bot.api.ChatId

case class Chat(id: ChatId, first_name: String, username: String, `type`: String)
