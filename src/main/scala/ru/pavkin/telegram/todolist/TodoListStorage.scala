package ru.pavkin.telegram.todolist

import cats.Functor
import fs2.async.Ref
import cats.implicits._
import ru.pavkin.telegram.api.ChatId

import scala.language.higherKinds

trait TodoListStorage[F[_]] {
  def addItem(chatId: ChatId, item: Item): F[Unit]
  def getItems(chatId: ChatId): F[List[Item]]
  def clearList(chatId: ChatId): F[Unit]
}

case class InMemoryTodoListStorage[F[_] : Functor](ref: Ref[F, Map[ChatId, List[Item]]]) extends TodoListStorage[F] {

  def addItem(chatId: ChatId, item: Item): F[Unit] =
    ref.modify(m => m.updated(chatId, item :: m.getOrElse(chatId, Nil))).void

  def getItems(chatId: ChatId): F[List[Item]] =
    ref.get.map(_.getOrElse(chatId, Nil))

  def clearList(chatId: ChatId): F[Unit] =
    ref.modify(_ - chatId).void
}
