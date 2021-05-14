package ru.pavkin.telegram.todolist

import cats.Functor
import cats.effect.concurrent.Ref
import cats.implicits._
import ru.pavkin.telegram.api.ChatId

/**
  * Algebra for managing storage of todo-list items
  */
trait TodoListStorage[F[_]] {
  def addItem(chatId: ChatId, item: Item): F[Unit]
  def getItems(chatId: ChatId): F[List[Item]]
  def clearList(chatId: ChatId): F[Unit]
}

/**
  * Simple in-memory implementation of [[TodoListStorage]] algebra, using [[Ref]].
  * In real world this would go to some database of sort.
  */
class InMemoryTodoListStorage[F[_]: Functor](
    private val ref: Ref[F, Map[ChatId, List[Item]]]
) extends TodoListStorage[F] {

  def addItem(chatId: ChatId, item: Item): F[Unit] =
    ref.update(m => m.updated(chatId, item :: m.getOrElse(chatId, Nil))).void

  def getItems(chatId: ChatId): F[List[Item]] =
    ref.get.map(_.getOrElse(chatId, Nil))

  def clearList(chatId: ChatId): F[Unit] =
    ref.update(_ - chatId).void
}
