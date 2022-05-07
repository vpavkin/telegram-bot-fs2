package ru.dm4x.dummy_tldr_bot.api.dto

import ru.dm4x.dummy_tldr_bot.api.FromId

case class From (id: FromId, is_bot: Boolean, first_name: String, username: String)
